
import { useState } from "react";

function Split({ pdfList = [], onSplitSuccess }) {

    const [splitPdfId, setSplitPdfId] = useState("");
    const [splitPages, setSplitPages] = useState("");
    const [outputPrefix, setOutputPrefix] = useState("split");
    const [message, setMessage] = useState("");
    const [splitFiles, setSplitFiles] = useState([]);
    const [isSplitting, setIsSplitting] = useState(false);

    // Deployed backend URL
    const API_URL = (
        import.meta.env.VITE_API_URL ||
        "https://smart-pdf-editor-backend.onrender.com"
    ).replace(/\/+$/, "");

    const handleSplit = async () => {

        if (!splitPdfId) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!splitPages.trim()) {
            setMessage("Please enter page numbers.");
            return;
        }

        if (!outputPrefix.trim()) {
            setMessage("Please enter an output prefix.");
            return;
        }

        const formData = new FormData();

        formData.append("pages", splitPages);
        formData.append("outputPrefix", outputPrefix);

        setIsSplitting(true);
        setMessage("");
        setSplitFiles([]);

        try {

            const response = await fetch(
                `${API_URL}/api/pdfs/split/${splitPdfId}`,
                {
                    method: "POST",
                    body: formData,
                    credentials: "include"
                }
            );

            const responseText = await response.text();

            let result;

            try {
                result = JSON.parse(responseText);
            } catch {
                result = responseText;
            }

            if (!response.ok) {
                throw new Error(
                    typeof result === "string"
                        ? result
                        : `Error splitting PDF (${response.status}).`
                );
            }

            if (!Array.isArray(result)) {
                throw new Error(
                    "Unexpected response from the backend."
                );
            }

            setSplitFiles(result);

            setMessage(
                `PDF split successfully! ${result.length} file(s) created.`
            );

            if (onSplitSuccess) {
                onSplitSuccess();
            }

            console.log("Split files:", result);

        } catch (error) {

            console.error("PDF split error:", error);

            setMessage(
                error.message || "Cannot connect to backend."
            );

            setSplitFiles([]);

        } finally {

            setIsSplitting(false);

        }

    };

    // Backend now returns filenames directly.
    const getFileName = (filePath) => {

        if (!filePath) {
            return "";
        }

        return filePath
            .replace(/\\/g, "/")
            .split("/")
            .pop();

    };

    const handleDownload = (filePath) => {

        const fileName = getFileName(filePath);

        if (!fileName) {
            return;
        }

        const downloadUrl =
            `${API_URL}/api/pdfs/download-merged/` +
            encodeURIComponent(fileName);

        window.open(
            downloadUrl,
            "_blank",
            "noopener,noreferrer"
        );

    };

    return (

        <div className="splitContainer">

            <div className="splitHeader">

                <h2>✂️ Split PDF</h2>

                <p>
                    Select a PDF and choose the pages you want to split.
                </p>

            </div>

            {/* Select PDF */}

            <select
                value={splitPdfId}
                onChange={(e) => {
                    setSplitPdfId(e.target.value);
                    setMessage("");
                    setSplitFiles([]);
                }}
                disabled={isSplitting}
            >

                <option value="">
                    Select PDF
                </option>

                {pdfList.map((pdf) => (

                    <option
                        key={pdf.id}
                        value={pdf.id}
                    >
                        {pdf.fileName}
                    </option>

                ))}

            </select>

            <br />
            <br />

            {/* Pages */}

            <input
                type="text"
                value={splitPages}
                onChange={(e) => setSplitPages(e.target.value)}
                placeholder="Pages (Example: 1-3 or 2,4)"
                disabled={isSplitting}
            />

            <br />
            <br />

            {/* Output Prefix */}

            <input
                type="text"
                value={outputPrefix}
                onChange={(e) => setOutputPrefix(e.target.value)}
                placeholder="Output Prefix"
                disabled={isSplitting}
            />

            <br />
            <br />

            <button
                onClick={handleSplit}
                disabled={isSplitting}
            >
                {isSplitting ? "Splitting..." : "Split PDF"}
            </button>

            {/* Message */}

            {message && (

                <p role="status" aria-live="polite">
                    {message}
                </p>

            )}

            {/* Generated Files */}

            {splitFiles.length > 0 && (

                <div>

                    <h3>Split Files</h3>

                    {splitFiles.map((filePath, index) => {

                        const fileName = getFileName(filePath);

                        return (

                            <div key={index}>

                                <span>
                                    {fileName}
                                </span>

                                {" "}

                                <button
                                    onClick={() =>
                                        handleDownload(filePath)
                                    }
                                >
                                    ⬇ Download
                                </button>

                            </div>

                        );

                    })}

                </div>

            )}

        </div>

    );

}

export default Split;
