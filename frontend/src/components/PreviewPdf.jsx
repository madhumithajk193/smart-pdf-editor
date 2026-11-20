
import { useState, useEffect } from "react";

const API_URL = import.meta.env.VITE_API_URL ||
    "http://localhost:8080";

function PreviewPdf({ pdfList = [] }) {

    const [selectedPdfId, setSelectedPdfId] = useState("");
    const [previewUrl, setPreviewUrl] = useState("");
    const [message, setMessage] = useState("");

    const selectedPdf = pdfList.find(
        (pdf) => String(pdf.id) === String(selectedPdfId)
    );

    useEffect(() => {

        let objectUrl;

        const loadPdf = async () => {

            if (!selectedPdf) {
                setPreviewUrl("");
                setMessage("");
                return;
            }

            try {
                setMessage("Loading PDF...");
                setPreviewUrl("");

                const response = await fetch(
                    `${API_URL}/api/pdfs/view/${selectedPdf.id}`,
                    {
                        method: "GET",
                        credentials: "include"
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        "Unable to load PDF. Status: " + response.status
                    );
                }

                const blob = await response.blob();

                if (blob.type && !blob.type.includes("pdf")) {
                    throw new Error("Server did not return a PDF.");
                }

                objectUrl = URL.createObjectURL(blob);

                setPreviewUrl(objectUrl);
                setMessage("");

            } catch (error) {
                console.error("Preview error:", error);
                setMessage(error.message);
            }
        };

        loadPdf();

        return () => {
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };

    }, [selectedPdfId, pdfList]);

    return (

        <div className="previewContainer">

            <div className="previewHeader">
                <h2>👁 PDF Preview</h2>
                <p>Select a PDF from your library.</p>
            </div>

            {/* PDF Dropdown */}
            <select
                value={selectedPdfId}
                onChange={(e) => setSelectedPdfId(e.target.value)}
            >
                <option value="">
                    Select a PDF
                </option>

                {pdfList.map((pdf) => (
                    <option key={pdf.id} value={pdf.id}>
                        {pdf.fileName}
                    </option>
                ))}
            </select>

            {/* Selected PDF Details */}
            {selectedPdf && (
                <div className="previewFileInfo">
                    <div className="previewFileIcon">📄</div>

                    <div>
                        <h3>{selectedPdf.fileName}</h3>
                        <p>PDF Document • ID: {selectedPdf.id}</p>
                    </div>
                </div>
            )}

            {/* PDF Viewer */}
            <div className="pdfViewer">

                {previewUrl ? (
                    <>
                        <button
                            className="openPreviewButton"
                            onClick={() => window.open(previewUrl, "_blank")}
                        >
                            ↗ Open Full Preview
                        </button>

                        <iframe
                            src={previewUrl}
                            title="PDF Preview"
                            width="100%"
                            height="650"
                        />
                    </>
                ) : (
                    <div className="previewEmpty">
                        <div className="previewIcon">📄</div>

                        <h2>
                            {pdfList.length === 0
                                ? "No PDFs Available"
                                : "Select a PDF"}
                        </h2>

                        <p>
                            {message ||
                                (pdfList.length === 0
                                    ? "Your PDF library is empty."
                                    : "Choose a PDF from the dropdown above.")}
                        </p>
                    </div>
                )}

            </div>

        </div>
    );
}

export default PreviewPdf;
