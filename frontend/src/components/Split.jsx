import { useState } from "react";

function Split({ pdfList, onSplitSuccess }) {

    const [splitPdfId, setSplitPdfId] = useState("");
    const [splitPages, setSplitPages] = useState("");
    const [outputPrefix, setOutputPrefix] = useState("split");
    const [message, setMessage] = useState("");
    const [splitFiles, setSplitFiles] = useState([]);

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

        try {

            const response = await fetch(
                `http://localhost:8080/api/pdfs/split/${splitPdfId}`,
                {
                    method: "POST",
                    body: formData
                }
            );

            const responseText = await response.text();

            let result;

            try {
                result = JSON.parse(responseText);
            } catch {
                result = responseText;
            }

            if (response.ok) {
                setMessage("PDF split successfully!");

                /*
                 * Backend returns a List<String>.
                 * Each string is the path of a generated PDF.
                 */
                setSplitFiles(result);

                if (onSplitSuccess) {
                    onSplitSuccess();
                }

                console.log("Split files:", result);

            } else {

                setMessage(
                    typeof result === "string"
                        ? result
                        : "Error splitting PDF."
                );

                setSplitFiles([]);

            }

        } catch (error) {

            console.error(error);

            setMessage("Cannot connect to backend.");

            setSplitFiles([]);

        }

    };

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

        window.open(
            `http://localhost:8080/api/pdfs/download-merged/${encodeURIComponent(fileName)}`,
            "_blank"
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
                onChange={(e) => setSplitPdfId(e.target.value)}
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
            />

            <br />
            <br />

            {/* Output Prefix */}

            <input
                type="text"
                value={outputPrefix}
                onChange={(e) => setOutputPrefix(e.target.value)}
                placeholder="Output Prefix"
            />

            <br />
            <br />

            <button onClick={handleSplit}>
                Split PDF
            </button>

            {/* Message */}

            {message && (

                <p>
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