import React, { useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function PdfBookmark({
                         pdfList = [],
                         onSuccess,
                         onBackHome
                     }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [bookmarkTitle, setBookmarkTitle] =
        useState("Employee Information");

    const [pageNumber, setPageNumber] =
        useState(1);

    const [outputFileName, setOutputFileName] =
        useState("bookmarked-result.pdf");

    const [loading, setLoading] =
        useState(false);

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");

    const [resultFileName, setResultFileName] =
        useState("");


    // =====================================================
    // ADD BOOKMARK
    // =====================================================

    const handleAddBookmark = async () => {

        setMessage("");
        setError("");
        setResultFileName("");

        // -------------------------------------------------
        // Validation
        // -------------------------------------------------

        if (!selectedPdf) {
            setError("Please select a PDF");
            return;
        }

        if (!bookmarkTitle.trim()) {
            setError("Please enter a bookmark title");
            return;
        }

        if (!pageNumber || pageNumber < 1) {
            setError("Page number must be at least 1");
            return;
        }

        if (!outputFileName.trim()) {
            setError("Please enter an output file name");
            return;
        }

        setLoading(true);

        try {

            // -------------------------------------------------
            // Make sure output has .pdf
            // -------------------------------------------------

            let finalOutputFileName =
                outputFileName.trim();

            if (
                !finalOutputFileName
                    .toLowerCase()
                    .endsWith(".pdf")
            ) {
                finalOutputFileName += ".pdf";
            }


            // -------------------------------------------------
            // Create URL parameters
            // -------------------------------------------------

            const params =
                new URLSearchParams();

            params.append(
                "bookmarkTitle",
                bookmarkTitle.trim()
            );

            params.append(
                "pageNumber",
                pageNumber
            );

            params.append(
                "outputFileName",
                finalOutputFileName
            );


            // -------------------------------------------------
            // API URL
            // -------------------------------------------------

            const url =
                `${API_BASE_URL}/api/pdfs/bookmark/${selectedPdf}?${params.toString()}`;

            console.log(
                "Add Bookmark URL:",
                url
            );


            // -------------------------------------------------
            // Send request
            // -------------------------------------------------

            const response =
                await fetch(url, {
                    method: "POST"
                });


            const text =
                await response.text();

            console.log(
                "Bookmark response:",
                text
            );


            // -------------------------------------------------
            // Parse response
            // -------------------------------------------------

            let result = null;

            try {
                result = JSON.parse(text);
            } catch {
                result = null;
            }


            // -------------------------------------------------
            // Handle error
            // -------------------------------------------------

            if (!response.ok) {

                throw new Error(
                    result?.message ||
                    result?.error ||
                    text ||
                    "Failed to add bookmark"
                );
            }


            // -------------------------------------------------
            // Success
            // -------------------------------------------------

            const generatedFileName =
                result?.fileName ||
                finalOutputFileName;


            setResultFileName(
                generatedFileName
            );


            setMessage(
                result?.message ||
                "Bookmark added successfully!"
            );


            console.log(
                "Generated bookmarked PDF:",
                generatedFileName
            );


            if (onSuccess) {
                onSuccess();
            }

        } catch (err) {

            console.error(
                "Bookmark error:",
                err
            );


            setError(
                err.message ||
                "Failed to add bookmark"
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // DOWNLOAD
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


        link.href =
            downloadUrl;


        link.download =
            resultFileName;


        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);
    };


    // =====================================================
    // BACK HOME
    // =====================================================

    const handleBackHome = () => {

        if (onBackHome) {
            onBackHome();
        }
    };


    // =====================================================
    // UI
    // =====================================================

    return (
        <div className="featurePage">

            {/* =========================================
                HEADER
            ========================================= */}

            <div className="featureHeader">

                <h1>
                    🔖 PDF Bookmarks
                </h1>

                <p>
                    Add a bookmark to a specific page
                    of your PDF document.
                </p>

            </div>


            {/* =========================================
                MAIN CARD
            ========================================= */}

            <div className="featureCard">

                <h2>
                    Add PDF Bookmark
                </h2>

                <p>
                    Select a PDF, enter a bookmark name
                    and choose the page where the bookmark
                    should open.
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
                    BOOKMARK TITLE
                ===================================== */}

                <div className="formGroup">

                    <label>
                        Bookmark Title
                    </label>

                    <input
                        type="text"
                        value={bookmarkTitle}
                        onChange={(e) =>
                            setBookmarkTitle(
                                e.target.value
                            )
                        }
                        placeholder="Employee Information"
                    />

                </div>


                {/* =====================================
                    PAGE NUMBER
                ===================================== */}

                <div className="formGroup">

                    <label>
                        Page Number
                    </label>

                    <input
                        type="number"
                        min="1"
                        value={pageNumber}
                        onChange={(e) =>
                            setPageNumber(
                                Number(e.target.value)
                            )
                        }
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
                        placeholder="bookmarked-result.pdf"
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
                    ADD BOOKMARK BUTTON
                ===================================== */}

                <button
                    className="primaryButton"
                    onClick={handleAddBookmark}
                    disabled={loading}
                >

                    {loading
                        ? "⏳ Adding Bookmark..."
                        : "🔖 Add Bookmark"}

                </button>


                {/* =====================================
                    DOWNLOAD
                ===================================== */}

                {resultFileName && (

                    <div className="resultActions">

                        <button
                            className="downloadButton"
                            onClick={handleDownload}
                        >

                            ⬇️ Download Bookmarked PDF

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

export default PdfBookmark;