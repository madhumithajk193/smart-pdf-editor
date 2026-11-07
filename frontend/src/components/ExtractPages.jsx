
import { useState } from "react";

function ExtractPages({ pdfList = [], onExtractSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [pages, setPages] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("extracted.pdf");

    const [message, setMessage] = useState("");
    const [extractedFileName, setExtractedFileName] =
        useState("");
    const [isExtracting, setIsExtracting] = useState(false);

    // Deployed backend URL
    const API_URL = (
        import.meta.env.VITE_API_URL ||
        "https://smart-pdf-editor-backend.onrender.com"
    ).replace(/\/+$/, "");

    const handleExtract = async () => {

        if (!selectedPdf) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!pages.trim()) {
            setMessage("Please enter the pages to extract.");
            return;
        }

        let fileName = outputFileName.trim() || "extracted.pdf";

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        if (/[<>:"/\\|?*\x00-\x1F]/.test(fileName)) {
            setMessage("Please enter a valid PDF filename.");
            return;
        }

        try {

            setIsExtracting(true);
            setMessage("");
            setExtractedFileName("");

            const formData = new FormData();

            formData.append("pages", pages);
            formData.append("outputFileName", fileName);

            const response = await fetch(
                `${API_URL}/api/pdfs/extract-pages/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData,
                    credentials: "include"
                }
            );

            const result = await response.text();

            if (!response.ok) {
                throw new Error(
                    result || `Extraction failed (${response.status}).`
                );
            }

            setMessage("PDF pages extracted successfully!");

            // Retain the requested filename.
           setExtractedFileName(result.trim());
            setPages("");

            if (onExtractSuccess) {
                onExtractSuccess();
            }

        } catch (error) {

            console.error("PDF extraction error:", error);

            setMessage(
                error.message || "Cannot connect to backend."
            );

            setExtractedFileName("");

        } finally {

            setIsExtracting(false);

        }
    };

    const handleDownload = () => {

        if (!extractedFileName) {
            return;
        }

        const downloadUrl =
            `${API_URL}/api/pdfs/download-merged/` +
            encodeURIComponent(extractedFileName);

        window.open(
            downloadUrl,
            "_blank",
            "noopener,noreferrer"
        );
    };

    return (

        <div className="extractContainer">

            <div className="extractHeader">

                <h2>📄 Extract Pages</h2>

                <p>
                    Select a PDF and choose the pages
                    you want to extract.
                </p>

            </div>

            <select
                value={selectedPdf}
                onChange={(e) => {
                    setSelectedPdf(e.target.value);
                    setMessage("");
                    setExtractedFileName("");
                }}
                disabled={isExtracting}
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

            <input
                type="text"
                value={pages}
                onChange={(e) => setPages(e.target.value)}
                placeholder="Pages (Example: 1,2,4)"
                disabled={isExtracting}
            />

            <br />
            <br />

            <input
                type="text"
                value={outputFileName}
                onChange={(e) =>
                    setOutputFileName(e.target.value)
                }
                placeholder="Output file name"
                disabled={isExtracting}
            />

            <br />
            <br />

            <button
                onClick={handleExtract}
                disabled={isExtracting}
            >
                {isExtracting
                    ? "Extracting..."
                    : "📄 Extract Pages"}
            </button>

            {message && (
                <p role="status" aria-live="polite">
                    {message}
                </p>
            )}

            {extractedFileName && (

                <div>

                    <p>
                        <b>{extractedFileName}</b> is ready.
                    </p>

                    <button onClick={handleDownload}>
                        ⬇ Download Extracted PDF
                    </button>

                </div>

            )}

        </div>
    );
}

export default ExtractPages;
