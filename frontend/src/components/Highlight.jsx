import React, { useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function Highlight({
                       pdfList = [],
                       onSuccess,
                       onBackHome
                   }) {

    // ============================================================
    // STATE
    // ============================================================

    const [selectedPdf, setSelectedPdf] =
        useState("");

    const [word, setWord] =
        useState("");

    const [outputFileName, setOutputFileName] =
        useState("highlighted.pdf");

    const [loading, setLoading] =
        useState(false);

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");

    const [resultFileName, setResultFileName] =
        useState("");


    // ============================================================
    // HIGHLIGHT WORD
    // ============================================================

    const handleHighlightWord = async () => {

        setMessage("");
        setError("");
        setResultFileName("");


        // --------------------------------------------------------
        // VALIDATION
        // --------------------------------------------------------

        if (!selectedPdf) {

            setError(
                "Please select a PDF."
            );

            return;
        }


        if (!word.trim()) {

            setError(
                "Please enter a word to highlight."
            );

            return;
        }


        if (!outputFileName.trim()) {

            setError(
                "Please enter an output file name."
            );

            return;
        }


        // --------------------------------------------------------
        // OUTPUT FILE NAME
        // --------------------------------------------------------

        let finalOutputFileName =
            outputFileName.trim();


        if (
            !finalOutputFileName
                .toLowerCase()
                .endsWith(".pdf")
        ) {

            finalOutputFileName += ".pdf";
        }


        setLoading(true);


        try {

            // ----------------------------------------------------
            // PARAMETERS
            // ----------------------------------------------------

            const params =
                new URLSearchParams();

            params.append(
                "word",
                word.trim()
            );

            params.append(
                "outputFileName",
                finalOutputFileName
            );


            // ----------------------------------------------------
            // API URL
            // ----------------------------------------------------

            const url =
                `${API_BASE_URL}/api/pdfs/highlight-word/${selectedPdf}?${params.toString()}`;


            console.log(
                "Highlight PDF URL:",
                url
            );


            // ----------------------------------------------------
            // REQUEST
            // ----------------------------------------------------

            const response =
                await fetch(
                    url,
                    {
                        method: "POST"
                    }
                );


            const text =
                await response.text();


            console.log(
                "Highlight PDF response:",
                text
            );


            // ----------------------------------------------------
            // ERROR
            // ----------------------------------------------------

            if (!response.ok) {

                throw new Error(
                    text ||
                    "PDF highlighting failed."
                );
            }


            // ----------------------------------------------------
            // SUCCESS
            // ----------------------------------------------------

            setResultFileName(
                finalOutputFileName
            );


            setMessage(
                text ||
                "Word highlighted PDF created successfully."
            );


            if (onSuccess) {
                onSuccess();
            }


        } catch (err) {

            console.error(
                "Highlight PDF error:",
                err
            );


            setError(
                err.message ||
                "Unable to highlight the PDF."
            );


        } finally {

            setLoading(false);

        }
    };


    // ============================================================
    // DOWNLOAD
    // ============================================================

    const handleDownload = () => {

        if (!resultFileName) {
            return;
        }


        const downloadUrl =
            `${API_BASE_URL}/api/pdfs/download-file/${encodeURIComponent(
                resultFileName
            )}`;


        console.log(
            "Download URL:",
            downloadUrl
        );


        const link =
            document.createElement("a");


        link.href =
            downloadUrl;


        link.download =
            resultFileName;


        document.body.appendChild(
            link
        );


        link.click();


        document.body.removeChild(
            link
        );
    };


    // ============================================================
    // BACK HOME
    // ============================================================

    const handleBackHome = () => {

        if (onBackHome) {

            onBackHome();

        }

    };


    // ============================================================
    // RENDER
    // ============================================================

    return (

        <div
            style={{
                minHeight: "100vh",
                width: "100%",
                boxSizing: "border-box",
                padding: "30px 20px 60px",
                background: "#0f172a",
                color: "#ffffff"
            }}
        >

            {/* ====================================================
                BACK BUTTON
            ==================================================== */}

            <button
                onClick={handleBackHome}
                style={{
                    background: "#2563eb",
                    color: "#ffffff",
                    border: "none",
                    borderRadius: "8px",
                    padding: "11px 20px",
                    fontSize: "15px",
                    fontWeight: "600",
                    cursor: "pointer",
                    marginBottom: "30px"
                }}
            >
                ← Back to Home
            </button>


            {/* ====================================================
                MAIN CONTAINER
            ==================================================== */}

            <div
                style={{
                    width: "100%",
                    maxWidth: "760px",
                    margin: "0 auto"
                }}
            >


                {/* =================================================
                    HEADER
                ================================================= */}

                <div
                    style={{
                        textAlign: "center",
                        marginBottom: "35px"
                    }}
                >

                    <div
                        style={{
                            fontSize: "58px",
                            marginBottom: "10px"
                        }}
                    >
                        🖍️
                    </div>


                    <h1
                        style={{
                            margin: "0 0 10px",
                            color: "#ffffff",
                            fontSize: "38px",
                            fontWeight: "800"
                        }}
                    >
                        Highlight PDF
                    </h1>


                    <p
                        style={{
                            margin: "0",
                            color: "#cbd5e1",
                            fontSize: "17px",
                            lineHeight: "1.6"
                        }}
                    >
                        Search for a word and highlight it
                        throughout the PDF.
                    </p>

                </div>


                {/* =================================================
                    FORM CARD
                ================================================= */}

                <div
                    style={{
                        background: "#172033",
                        border: "1px solid #334155",
                        borderRadius: "16px",
                        padding: "30px",
                        boxShadow:
                            "0 10px 30px rgba(0,0,0,0.25)"
                    }}
                >


                    {/* =================================================
                        SELECT PDF
                    ================================================= */}

                    <div
                        style={{
                            marginBottom: "24px"
                        }}
                    >

                        <label
                            style={{
                                display: "block",
                                marginBottom: "9px",
                                color: "#ffffff",
                                fontSize: "16px",
                                fontWeight: "700"
                            }}
                        >
                            📄 Select PDF
                        </label>


                        <select
                            value={selectedPdf}
                            onChange={(e) => {

                                setSelectedPdf(
                                    e.target.value
                                );

                                setMessage("");
                                setError("");
                                setResultFileName("");

                            }}
                            style={{
                                width: "100%",
                                boxSizing: "border-box",
                                padding: "13px 14px",
                                borderRadius: "8px",
                                border: "1px solid #64748b",
                                background: "#ffffff",
                                color: "#111827",
                                fontSize: "15px",
                                outline: "none",
                                cursor: "pointer"
                            }}
                        >

                            <option value="">
                                -- Select a PDF --
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

                    </div>


                    {/* =================================================
                        WORD
                    ================================================= */}

                    <div
                        style={{
                            marginBottom: "24px"
                        }}
                    >

                        <label
                            style={{
                                display: "block",
                                marginBottom: "9px",
                                color: "#ffffff",
                                fontSize: "16px",
                                fontWeight: "700"
                            }}
                        >
                            🔍 Word to Highlight
                        </label>


                        <input
                            type="text"
                            value={word}
                            onChange={(e) => {

                                setWord(
                                    e.target.value
                                );

                                setMessage("");
                                setError("");

                            }}
                            placeholder="Enter a word, for example: Resume"
                            style={{
                                width: "100%",
                                boxSizing: "border-box",
                                padding: "13px 14px",
                                borderRadius: "8px",
                                border: "1px solid #64748b",
                                background: "#ffffff",
                                color: "#111827",
                                fontSize: "15px",
                                outline: "none"
                            }}
                        />

                    </div>


                    {/* =================================================
                        OUTPUT FILE
                    ================================================= */}

                    <div
                        style={{
                            marginBottom: "28px"
                        }}
                    >

                        <label
                            style={{
                                display: "block",
                                marginBottom: "9px",
                                color: "#ffffff",
                                fontSize: "16px",
                                fontWeight: "700"
                            }}
                        >
                            💾 Output File Name
                        </label>


                        <input
                            type="text"
                            value={outputFileName}
                            onChange={(e) => {

                                setOutputFileName(
                                    e.target.value
                                );

                            }}
                            placeholder="highlighted.pdf"
                            style={{
                                width: "100%",
                                boxSizing: "border-box",
                                padding: "13px 14px",
                                borderRadius: "8px",
                                border: "1px solid #64748b",
                                background: "#ffffff",
                                color: "#111827",
                                fontSize: "15px",
                                outline: "none"
                            }}
                        />

                    </div>


                    {/* =================================================
                        HIGHLIGHT BUTTON
                    ================================================= */}

                    <button
                        onClick={
                            handleHighlightWord
                        }
                        disabled={loading}
                        style={{
                            width: "100%",
                            padding: "14px 20px",
                            borderRadius: "9px",
                            border: "none",
                            background:
                                loading
                                    ? "#64748b"
                                    : "#2563eb",
                            color: "#ffffff",
                            fontSize: "16px",
                            fontWeight: "700",
                            cursor:
                                loading
                                    ? "not-allowed"
                                    : "pointer"
                        }}
                    >

                        {loading
                            ? "⏳ Highlighting..."
                            : "🖍️ Highlight Word"
                        }

                    </button>


                    {/* =================================================
                        ERROR
                    ================================================= */}

                    {error && (

                        <div
                            style={{
                                marginTop: "20px",
                                padding: "14px 16px",
                                borderRadius: "8px",
                                background: "#450a0a",
                                border: "1px solid #ef4444",
                                color: "#fecaca",
                                fontSize: "15px",
                                lineHeight: "1.5"
                            }}
                        >
                            ❌ {error}
                        </div>

                    )}


                    {/* =================================================
                        SUCCESS
                    ================================================= */}

                    {message && (

                        <div
                            style={{
                                marginTop: "20px",
                                padding: "16px",
                                borderRadius: "8px",
                                background: "#052e16",
                                border: "1px solid #22c55e",
                                color: "#bbf7d0",
                                fontSize: "15px",
                                lineHeight: "1.5"
                            }}
                        >
                            ✅ {message}
                        </div>

                    )}


                    {/* =================================================
                        DOWNLOAD
                    ================================================= */}

                    {resultFileName && (

                        <div
                            style={{
                                marginTop: "22px",
                                textAlign: "center"
                            }}
                        >

                            <p
                                style={{
                                    marginBottom: "12px",
                                    color: "#e2e8f0",
                                    fontSize: "15px"
                                }}
                            >
                                📄 Generated file:
                                <strong
                                    style={{
                                        color: "#ffffff",
                                        marginLeft: "6px"
                                    }}
                                >
                                    {resultFileName}
                                </strong>
                            </p>


                            <button
                                onClick={
                                    handleDownload
                                }
                                style={{
                                    padding: "12px 22px",
                                    borderRadius: "8px",
                                    border:
                                        "1px solid #60a5fa",
                                    background: "#1d4ed8",
                                    color: "#ffffff",
                                    fontSize: "15px",
                                    fontWeight: "700",
                                    cursor: "pointer"
                                }}
                            >
                                ⬇️ Download Highlighted PDF
                            </button>

                        </div>

                    )}

                </div>

            </div>

        </div>
    );
}

export default Highlight;