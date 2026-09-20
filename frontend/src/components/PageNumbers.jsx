import { useState } from "react";

function PageNumbers({ pdfList, onPageNumberSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");

    const [startingPageNumber, setStartingPageNumber] =
        useState("1");

    const [outputFileName, setOutputFileName] =
        useState("numbered.pdf");

    const [loading, setLoading] =
        useState(false);

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");

    const [downloadReady, setDownloadReady] =
        useState(false);


    /* =====================================================
       ADD PAGE NUMBERS
    ===================================================== */

    const handlePageNumbers = async () => {

        setMessage("");
        setError("");
        setDownloadReady(false);

        if (!selectedPdf) {

            setError("Please select a PDF.");

            return;
        }


        if (
            !startingPageNumber ||
            Number(startingPageNumber) < 1
        ) {

            setError(
                "Starting page number must be 1 or greater."
            );

            return;
        }


        if (!outputFileName.trim()) {

            setError(
                "Please enter an output file name."
            );

            return;
        }


        setLoading(true);


        try {

            const params = new URLSearchParams();

            params.append(
                "startingPageNumber",
                startingPageNumber
            );

            params.append(
                "outputFileName",
                outputFileName
            );


            const response = await fetch(

                `http://localhost:8080/api/pdfs/add-page-numbers/${selectedPdf}`,

                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body: params.toString()
                }

            );


            const result =
                await response.text();


            if (!response.ok) {

                throw new Error(result);

            }


            setMessage(
                "Page numbers added successfully!"
            );

            setDownloadReady(true);


            if (onPageNumberSuccess) {

                onPageNumberSuccess();

            }


        } catch (err) {

            console.error(
                "Page numbering error:",
                err
            );


            setError(
                err.message ||
                "Failed to add page numbers."
            );


        } finally {

            setLoading(false);

        }

    };


    /* =====================================================
       RENDER
    ===================================================== */

    return (

        <div className="featureContainer">

            <h2>
                🔢 Add Page Numbers
            </h2>


            <p>
                Add page numbers to your PDF document.
            </p>


            {/* =================================================
                SELECT PDF
            ================================================= */}

            <div className="formGroup">

                <label>
                    📄 Select PDF
                </label>


                <select
                    value={selectedPdf}
                    onChange={(e) =>
                        setSelectedPdf(e.target.value)
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

                        ))
                    }

                </select>

            </div>


            {/* =================================================
                STARTING PAGE NUMBER
            ================================================= */}

            <div className="formGroup">

                <label>
                    🔢 Starting Number
                </label>


                <input
                    type="number"
                    min="1"
                    value={startingPageNumber}
                    onChange={(e) =>
                        setStartingPageNumber(
                            e.target.value
                        )
                    }
                />

            </div>


            {/* =================================================
                OUTPUT FILE NAME
            ================================================= */}

            <div className="formGroup">

                <label>
                    💾 Output File Name
                </label>


                <input
                    type="text"
                    value={outputFileName}
                    onChange={(e) =>
                        setOutputFileName(
                            e.target.value
                        )
                    }
                    placeholder="numbered.pdf"
                />

            </div>


            {/* =================================================
                ADD BUTTON
            ================================================= */}

            <button
                className="actionButton"
                onClick={handlePageNumbers}
                disabled={loading}
            >

                {loading
                    ? "⏳ Adding..."
                    : "🔢 Add Page Numbers"
                }

            </button>


            {/* =================================================
                ERROR
            ================================================= */}

            {error && (

                <div className="errorMessage">

                    ❌ {error}

                </div>

            )}


            {/* =================================================
                SUCCESS + DOWNLOAD
            ================================================= */}

            {message && (

                <div className="successMessage">

                    ✅ {message}


                    {downloadReady && (

                        <div
                            style={{
                                marginTop: "15px"
                            }}
                        >

                            <a
                                href={
                                    `http://localhost:8080/api/pdfs/download-file/${outputFileName}`
                                }
                                download
                                className="downloadButton"
                            >

                                ⬇️ Download Numbered PDF

                            </a>

                        </div>

                    )}

                </div>

            )}

        </div>

    );

}

export default PageNumbers;