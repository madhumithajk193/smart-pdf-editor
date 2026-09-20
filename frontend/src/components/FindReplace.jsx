import React, { useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function FindReplace({ pdfList = [], onSuccess, onBackHome }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [searchText, setSearchText] = useState("");
    const [replaceText, setReplaceText] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("find-replace-result.pdf");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    // Store generated PDF name
    const [resultFileName, setResultFileName] = useState("");

    const handleFindReplace = async () => {

        setMessage("");
        setError("");
        setResultFileName("");

        if (!selectedPdf) {
            setError("Please select a PDF");
            return;
        }

        if (!searchText.trim()) {
            setError("Please enter text to find");
            return;
        }

        if (!outputFileName.trim()) {
            setError("Please enter an output file name");
            return;
        }

        setLoading(true);

        try {

            const params = new URLSearchParams();

            params.append(
                "searchText",
                searchText.trim()
            );

            params.append(
                "replaceText",
                replaceText.trim()
            );

            params.append(
                "outputFileName",
                outputFileName.trim()
            );

            const url =
                `${API_BASE_URL}/api/pdfs/find-replace/${selectedPdf}?${params.toString()}`;

            console.log(
                "Find & Replace URL:",
                url
            );

            const response = await fetch(url, {
                method: "POST"
            });

            const text = await response.text();

            console.log(
                "Backend response:",
                text
            );

            let result = null;

            try {
                result = JSON.parse(text);
            } catch {
                result = null;
            }

            if (!response.ok) {

                throw new Error(
                    result?.message ||
                    result?.error ||
                    text ||
                    "Find & Replace failed"
                );
            }

            // ---------------------------------------------
            // SUCCESS
            // ---------------------------------------------

            const generatedFileName =
                result?.fileName ||
                outputFileName.trim();

            setResultFileName(
                generatedFileName
            );

            setMessage(
                result?.message ||
                "Find & Replace completed successfully!"
            );

            console.log(
                "Generated PDF:",
                generatedFileName
            );

            if (onSuccess) {
                onSuccess();
            }

        } catch (err) {

            console.error(
                "Find & Replace error:",
                err
            );

            setError(
                err.message ||
                "Find & Replace failed"
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // DOWNLOAD GENERATED PDF
    // =====================================================

    const handleDownload = () => {

        if (!resultFileName) {
            return;
        }

        const downloadUrl =
            `${API_BASE_URL}/api/pdfs/download-file/${encodeURIComponent(
                resultFileName
            )}`;

        console.log(
            "Downloading:",
            downloadUrl
        );

        const link =
            document.createElement("a");

        link.href = downloadUrl;

        link.download =
            resultFileName;

        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);
    };


    // =====================================================
    // BACK TO HOME
    // =====================================================

    const handleBackHome = () => {

        if (onBackHome) {
            onBackHome();
        }
    };


    return (
        <div className="featurePage">

            {/* =========================================
                HEADER
            ========================================= */}

            <div className="featureHeader">

                <h1>
                    🔍 Find & Replace
                </h1>

                <p>
                    Find text in your PDF and replace it
                    with new text.
                </p>

            </div>


            {/* =========================================
                MAIN CARD
            ========================================= */}

            <div className="featureCard">

                <h2>
                    Find & Replace PDF
                </h2>

                <p>
                    Select a PDF, enter the text to find
                    and provide the replacement text.
                </p>


                {/* =====================================
                    SELECT PDF
                ===================================== */}

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


                {/* =====================================
                    FIND TEXT
                ===================================== */}

                <div className="formGroup">

                    <label>
                        Find Text
                    </label>

                    <input
                        type="text"
                        value={searchText}
                        onChange={(e) =>
                            setSearchText(
                                e.target.value
                            )
                        }
                        placeholder="Enter text to find"
                    />

                </div>


                {/* =====================================
                    REPLACE TEXT
                ===================================== */}

                <div className="formGroup">

                    <label>
                        Replace With
                    </label>

                    <input
                        type="text"
                        value={replaceText}
                        onChange={(e) =>
                            setReplaceText(
                                e.target.value
                            )
                        }
                        placeholder="Enter replacement text"
                    />

                </div>


                {/* =====================================
                    OUTPUT FILE
                ===================================== */}

                <div className="formGroup">

                    <label>
                        Output File Name
                    </label>

                    <input
                        type="text"
                        value={outputFileName}
                        onChange={(e) =>
                            setOutputFileName(
                                e.target.value
                            )
                        }
                        placeholder="find-replace-result.pdf"
                    />

                </div>


                {/* =====================================
                    ERROR
                ===================================== */}

                {error && (

                    <div className="errorMessage">

                        ❌ {error}

                    </div>

                )}


                {/* =====================================
                    SUCCESS
                ===================================== */}

                {message && (

                    <div className="successMessage">

                        ✅ {message}

                    </div>

                )}


                {/* =====================================
                    FIND & REPLACE BUTTON
                ===================================== */}

                <button
                    className="primaryButton"
                    onClick={handleFindReplace}
                    disabled={loading}
                >

                    {loading
                        ? "⏳ Processing..."
                        : "🔍 Find & Replace"}

                </button>


                {/* =====================================
                    DOWNLOAD BUTTON
                ===================================== */}

                {resultFileName && (

                    <div className="resultActions">

                        <button
                            className="downloadButton"
                            onClick={handleDownload}
                        >

                            ⬇️ Download Result PDF

                        </button>

                        <p className="resultFileName">

                            📄 {resultFileName}

                        </p>

                    </div>

                )}




            </div>

        </div>
    );
}

export default FindReplace;