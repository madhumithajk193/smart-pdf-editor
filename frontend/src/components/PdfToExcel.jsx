import React, { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080/api/pdfs";

export default function PdfToExcel() {
    const [pdfs, setPdfs] = useState([]);
    const [selectedPdfId, setSelectedPdfId] = useState("");
    const [selectedFileName, setSelectedFileName] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    useEffect(() => {
        loadPdfs();
    }, []);

    const loadPdfs = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}`);

            if (!response.ok) {
                throw new Error("Failed to load PDFs");
            }

            const data = await response.json();

            setPdfs(data);

            if (data.length > 0) {
                setSelectedPdfId(data[0].id);
                setSelectedFileName(data[0].fileName);
            }

        } catch (error) {
            console.error("Error loading PDFs:", error);
            setMessage("Failed to load PDF files.");
        }
    };

    const handlePdfChange = (event) => {
        const id = event.target.value;

        setSelectedPdfId(id);

        const selectedPdf = pdfs.find(
            (pdf) => String(pdf.id) === String(id)
        );

        if (selectedPdf) {
            setSelectedFileName(selectedPdf.fileName);
        }
    };

    const handleConvert = async () => {
        if (!selectedPdfId) {
            setMessage("Please select a PDF file.");
            return;
        }

        setLoading(true);
        setMessage("");

        try {
            const outputFileName =
                selectedFileName.replace(/\.pdf$/i, "") + ".xlsx";

            console.log("Converting PDF ID:", selectedPdfId);
            console.log("Output filename:", outputFileName);

            const response = await fetch(
                `${API_BASE_URL}/to-excel/${selectedPdfId}?outputFileName=${encodeURIComponent(
                    outputFileName
                )}`,
                {
                    method: "POST",
                }
            );

            if (!response.ok) {
                const errorText = await response.text();

                throw new Error(
                    errorText || "PDF to Excel conversion failed"
                );
            }

            /*
             * IMPORTANT:
             * The backend directly returns the Excel file.
             * We must read the response as a Blob.
             */
            const blob = await response.blob();

            if (!blob || blob.size === 0) {
                throw new Error("The generated Excel file is empty.");
            }

            console.log("Excel Blob size:", blob.size);
            console.log("Excel Blob type:", blob.type);

            /*
             * Create a temporary browser URL for the Blob.
             */
            const downloadUrl = window.URL.createObjectURL(blob);

            /*
             * Create temporary download link.
             */
            const link = document.createElement("a");

            link.href = downloadUrl;
            link.download = outputFileName;

            document.body.appendChild(link);

            link.click();

            document.body.removeChild(link);

            /*
             * Release temporary URL.
             */
            window.URL.revokeObjectURL(downloadUrl);

            setMessage(
                `Excel file downloaded successfully: ${outputFileName}`
            );

        } catch (error) {

            console.error("PDF TO EXCEL ERROR:", error);

            setMessage(
                error.message ||
                "Excel file was created, but the download failed."
            );

        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            style={{
                width: "100%",
                maxWidth: "460px",
                margin: "0 auto",
                padding: "20px",
                borderRadius: "20px",
                border: "1px solid rgba(255,255,255,0.15)",
                background: "#111a30",
                color: "white",
            }}
        >

            <div
                style={{
                    textAlign: "center",
                    fontSize: "48px",
                    marginBottom: "5px",
                }}
            >
                📊
            </div>

            <h2
                style={{
                    textAlign: "center",
                    margin: "0",
                }}
            >
                PDF to Excel
            </h2>

            <p
                style={{
                    textAlign: "center",
                    color: "#d0d7e8",
                    marginBottom: "30px",
                }}
            >
                Convert PDF tables into an Excel spreadsheet.
            </p>

            <label
                style={{
                    display: "block",
                    fontWeight: "600",
                    marginBottom: "10px",
                }}
            >
                Select a PDF file
            </label>

            <select
                value={selectedPdfId}
                onChange={handlePdfChange}
                style={{
                    width: "100%",
                    padding: "14px",
                    borderRadius: "10px",
                    border: "1px solid #52617a",
                    background: "#18243b",
                    color: "white",
                    fontSize: "15px",
                    marginBottom: "20px",
                }}
            >
                {pdfs.length === 0 ? (
                    <option value="">
                        No PDF files available
                    </option>
                ) : (
                    pdfs.map((pdf) => (
                        <option
                            key={pdf.id}
                            value={pdf.id}
                        >
                            {pdf.fileName}
                        </option>
                    ))
                )}
            </select>

            {selectedFileName && (
                <div
                    style={{
                        background: "#172a4f",
                        padding: "14px",
                        borderRadius: "10px",
                        marginBottom: "20px",
                    }}
                >
                    Selected file:{" "}
                    <strong>{selectedFileName}</strong>
                </div>
            )}

            <button
                onClick={handleConvert}
                disabled={loading || !selectedPdfId}
                style={{
                    width: "100%",
                    padding: "14px",
                    border: "none",
                    borderRadius: "10px",
                    background: loading
                        ? "#52617a"
                        : "#2864e8",
                    color: "white",
                    fontSize: "16px",
                    fontWeight: "600",
                    cursor:
                        loading || !selectedPdfId
                            ? "not-allowed"
                            : "pointer",
                }}
            >
                {loading
                    ? "⏳ Converting..."
                    : "📊 Convert to Excel"}
            </button>

            {message && (
                <div
                    style={{
                        marginTop: "20px",
                        padding: "14px",
                        borderRadius: "10px",
                        background:
                            message.toLowerCase().includes("successfully") ||
                            message.toLowerCase().includes("downloaded")
                                ? "#123c2b"
                                : "#431c32",
                        color: "white",
                    }}
                >
                    {message}
                </div>
            )}

        </div>
    );
}