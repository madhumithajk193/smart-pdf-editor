import { useState } from "react";

function OcrPdf({ pdfList }) {

    const [selectedPdf, setSelectedPdf] =
        useState("");

    const [language, setLanguage] =
        useState("eng");

    const [results, setResults] =
        useState([]);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    const [completed, setCompleted] =
        useState(false);


    // =====================================================
    // OCR PDF
    // =====================================================

    const handleOcr = async () => {

        setError("");
        setResults([]);
        setCompleted(false);

        if (!selectedPdf) {

            setError(
                "Please select a PDF first."
            );

            return;
        }

        setLoading(true);

        try {

            const response =
                await fetch(
                    `http://localhost:8080/api/pdfs/ocr/${selectedPdf}?language=${language}`,
                    {
                        method: "POST"
                    }
                );

            const data =
                await response.json();

            if (!response.ok) {

                throw new Error(
                    typeof data === "string"
                        ? data
                        : "OCR failed"
                );
            }

            setResults(data);

            setCompleted(true);

        } catch (err) {

            console.error(
                "OCR Error:",
                err
            );

            setError(
                err.message ||
                "OCR failed"
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // Get Complete Text
    // =====================================================

    const getFullText = () => {

        return results
            .map(
                page =>
                    `===== PAGE ${page.page} =====\n\n${page.text}`
            )
            .join("\n\n");
    };


    // =====================================================
    // Copy Text
    // =====================================================

    const copyText = async () => {

        const text =
            getFullText();

        if (!text) {
            return;
        }

        try {

            await navigator.clipboard.writeText(
                text
            );

            alert(
                "OCR text copied successfully."
            );

        } catch (error) {

            console.error(
                "Copy failed:",
                error
            );

        }
    };


    // =====================================================
    // Download Text
    // =====================================================

    const downloadText = () => {

        const text =
            getFullText();

        if (!text) {
            return;
        }

        const blob =
            new Blob(
                [text],
                {
                    type: "text/plain"
                }
            );

        const url =
            URL.createObjectURL(blob);

        const link =
            document.createElement("a");

        link.href = url;

        link.download =
            "ocr-result.txt";

        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);

        URL.revokeObjectURL(url);
    };


    return (

        <div className="toolContainer">

            {/* =================================================
                HEADER
            ================================================= */}

            <div className="toolHeader">

                <div>

                    <h2>
                        🔍 OCR PDF
                    </h2>

                    <p>
                        Extract text from scanned
                        and image-based PDF documents.
                    </p>

                </div>

            </div>


            {/* =================================================
                FORM
            ================================================= */}

            <div className="toolCard">

                <div className="formGroup">

                    <label>
                        Select PDF
                    </label>

                    <select
                        value={selectedPdf}
                        onChange={(e) =>
                            setSelectedPdf(
                                e.target.value
                            )
                        }
                    >

                        <option value="">
                            -- Select PDF --
                        </option>

                        {pdfList &&
                            pdfList.map((pdf) => (

                                <option
                                    key={pdf.id}
                                    value={pdf.id}
                                >
                                    {pdf.fileName}
                                </option>

                            ))}

                    </select>

                </div>


                <div className="formGroup">

                    <label>
                        OCR Language
                    </label>

                    <select
                        value={language}
                        onChange={(e) =>
                            setLanguage(
                                e.target.value
                            )
                        }
                    >

                        <option value="eng">
                            English
                        </option>

                    </select>

                </div>


                <button
                    className="primaryButton"
                    onClick={handleOcr}
                    disabled={loading}
                >

                    {loading
                        ? "🔄 Processing OCR..."
                        : "🔍 Extract Text"}

                </button>

            </div>


            {/* =================================================
                ERROR
            ================================================= */}

            {error && (

                <div className="errorMessage">

                    ❌ {error}

                </div>

            )}


            {/* =================================================
                SUCCESS
            ================================================= */}

            {completed && (

                <div className="successMessage">

                    ✅ OCR completed successfully.

                    <br />

                    {results.length} page(s)
                    processed.

                </div>

            )}


            {/* =================================================
                RESULT ACTIONS
            ================================================= */}

            {results.length > 0 && (

                <div className="ocrActions">

                    <button
                        className="secondaryButton"
                        onClick={copyText}
                    >
                        📋 Copy All Text
                    </button>

                    <button
                        className="secondaryButton"
                        onClick={downloadText}
                    >
                        ⬇️ Download TXT
                    </button>

                </div>

            )}


            {/* =================================================
                OCR RESULTS
            ================================================= */}

            {results.length > 0 && (

                <div className="ocrResults">

                    <h3>
                        📄 OCR Results
                    </h3>


                    {results.map((page) => (

                        <div
                            className="ocrPage"
                            key={page.page}
                        >

                            <div className="ocrPageHeader">

                                <strong>
                                    Page {page.page}
                                </strong>

                                <span>
                                    {page.characterCount}
                                    {" "}
                                    characters
                                </span>

                            </div>


                            <textarea
                                className="ocrText"
                                value={page.text}
                                readOnly
                            />

                        </div>

                    ))}

                </div>

            )}

        </div>
    );
}

export default OcrPdf;