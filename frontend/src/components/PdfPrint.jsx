import React, { useState } from "react";

const API_BASE = "http://localhost:8080";

function PdfPrint({ pdfList = [] }) {

    const [selectedPdfId, setSelectedPdfId] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // ============================================================
    // GET PDF
    // ============================================================

    const getPdfUrl = () => {

        if (!selectedPdfId) {
            return "";
        }

        return `${API_BASE}/api/pdfs/view/${selectedPdfId}`;
    };


    // ============================================================
    // PRINT PDF
    // ============================================================

    const printPdf = () => {

        if (!selectedPdfId) {

            setError(
                "Please select a PDF."
            );

            return;
        }

        setError("");
        setLoading(true);

        const pdfUrl = getPdfUrl();

        const printWindow =
            window.open(
                pdfUrl,
                "_blank"
            );

        if (!printWindow) {

            setError(
                "Popup was blocked. Please allow popups for this website."
            );

            setLoading(false);

            return;
        }

        // Give the PDF viewer time to load
        setTimeout(() => {

            try {

                printWindow.focus();

                printWindow.print();

            } catch (err) {

                console.error(err);

                setError(
                    "Unable to open the print dialog."
                );

            } finally {

                setLoading(false);
            }

        }, 2500);
    };


    // ============================================================
    // PDF NAME
    // ============================================================

    const getPdfName = (pdf) => {

        return (
            pdf.fileName ||
            pdf.filename ||
            pdf.name ||
            `PDF ${pdf.id}`
        );
    };


    // ============================================================
    // RENDER
    // ============================================================

    return (
        <div className="featureContainer">

            {/* ================================================== */}
            {/* HEADER */}
            {/* ================================================== */}

            <div className="featureHeader">

                <div className="featureIcon">
                    🖨️
                </div>

                <div>

                    <h1>
                        Print PDF
                    </h1>

                    <p>
                        Select a PDF and print it directly.
                    </p>

                </div>

            </div>


            {/* ================================================== */}
            {/* SELECT PDF */}
            {/* ================================================== */}

            <div className="featureCard">

                <h2>
                    Select PDF
                </h2>


                {pdfList.length === 0 ? (

                    <div className="emptyMessage">
                        No uploaded PDFs available.
                    </div>

                ) : (

                    <select
                        className="pdfSelect"
                        value={selectedPdfId}
                        onChange={(e) => {

                            setSelectedPdfId(
                                e.target.value
                            );

                            setError("");

                        }}
                    >

                        <option value="">
                            Select a PDF
                        </option>

                        {pdfList.map((pdf) => (

                            <option
                                key={pdf.id}
                                value={pdf.id}
                            >
                                {getPdfName(pdf)}
                            </option>

                        ))}

                    </select>

                )}


                {/* ================================================== */}
                {/* PRINT BUTTON */}
                {/* ================================================== */}

                <button
                    className="primaryButton"
                    onClick={printPdf}
                    disabled={
                        loading ||
                        !selectedPdfId
                    }
                >

                    {loading
                        ? "Opening Print..."
                        : "🖨️ Print PDF"
                    }

                </button>


                {/* ================================================== */}
                {/* ERROR */}
                {/* ================================================== */}

                {error && (

                    <div className="errorMessage">
                        ❌ {error}
                    </div>

                )}

            </div>


            {/* ================================================== */}
            {/* INFORMATION */}
            {/* ================================================== */}

            <div className="featureCard">

                <h2>
                    How it works
                </h2>

                <p>
                    1. Select an uploaded PDF.
                </p>

                <p>
                    2. Click <strong>Print PDF</strong>.
                </p>

                <p>
                    3. The PDF opens in a new window.
                </p>

                <p>
                    4. The browser print dialog will appear.
                </p>

                <p>
                    5. Select your printer and print.
                </p>

            </div>

        </div>
    );
}

export default PdfPrint;