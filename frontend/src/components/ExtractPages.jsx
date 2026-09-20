import { useState } from "react";

function ExtractPages({ pdfList, onExtractSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [pages, setPages] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("extracted.pdf");

    const [message, setMessage] = useState("");
    const [extractedFileName, setExtractedFileName] =
        useState("");

    const handleExtract = async () => {

        if (!selectedPdf) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!pages.trim()) {
            setMessage("Please enter the pages to extract.");
            return;
        }

        let fileName = outputFileName.trim();

        if (!fileName) {
            fileName = "extracted.pdf";
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }

        try {

            const formData = new FormData();

            formData.append("pages", pages);
            formData.append(
                "outputFileName",
                fileName
            );

            const response = await fetch(
                `http://localhost:8080/api/pdfs/extract-pages/${selectedPdf}`,
                {
                    method: "POST",
                    body: formData
                }
            );

            const result = await response.text();

            if (response.ok) {

                setMessage(
                    "PDF pages extracted successfully!"
                );

                setExtractedFileName(fileName);

                setPages("");

                if (onExtractSuccess) {
                    onExtractSuccess();
                }

            } else {

                setMessage(
                    "Error extracting pages: " + result
                );

                setExtractedFileName("");
            }

        } catch (error) {

            console.error(error);

            setMessage(
                "Cannot connect to backend."
            );

            setExtractedFileName("");
        }
    };

    const handleDownload = () => {

        if (!extractedFileName) {
            return;
        }

        window.open(
            `http://localhost:8080/api/pdfs/download-merged/${encodeURIComponent(extractedFileName)}`,
            "_blank"
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
                onChange={(e) =>
                    setSelectedPdf(e.target.value)
                }
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
                onChange={(e) =>
                    setPages(e.target.value)
                }
                placeholder="Pages (Example: 1,2,4)"
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
            />

            <br />
            <br />

            <button onClick={handleExtract}>
                📄 Extract Pages
            </button>

            {message && (
                <p>{message}</p>
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