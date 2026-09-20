import { useEffect, useState } from "react";

function WordToPdf({ pdfList = [] }) {

    const [selectedWord, setSelectedWord] = useState("");
    const [outputFileName, setOutputFileName] =
        useState("converted.pdf");

    const [message, setMessage] = useState("");
    const [downloadReady, setDownloadReady] =
        useState(false);


    // ==========================================
    // GET ONLY WORD FILES
    // ==========================================

    const wordFiles = Array.isArray(pdfList)
        ? pdfList.filter((file) =>
            (file.fileName || file.file_name || "")
                .toLowerCase()
                .endsWith(".docx")
        )
        : [];


    // ==========================================
    // AUTOMATICALLY SELECT FIRST WORD FILE
    // ==========================================

    useEffect(() => {

        if (wordFiles.length > 0 && !selectedWord) {

            setSelectedWord(
                String(wordFiles[0].id)
            );

        }

    }, [wordFiles.length, selectedWord]);


    // ==========================================
    // CONVERT WORD TO PDF
    // ==========================================

    const handleConvert = async () => {

        if (!selectedWord) {

            setMessage(
                "❌ Please select a Word file"
            );

            return;
        }


        if (!outputFileName.trim()) {

            setMessage(
                "❌ Please enter an output PDF name"
            );

            return;
        }


        try {

            setMessage(
                "⏳ Converting Word to PDF..."
            );

            setDownloadReady(false);


            const response = await fetch(
                `http://localhost:8080/api/pdfs/word-to-pdf/${selectedWord}?outputFileName=${encodeURIComponent(
                    outputFileName.trim()
                )}`,
                {
                    method: "POST"
                }
            );


            if (!response.ok) {

                const errorText =
                    await response.text();

                throw new Error(
                    errorText ||
                    "Conversion failed"
                );
            }


            await response.text();


            setMessage(
                "✅ Word converted to PDF successfully!"
            );

            setDownloadReady(true);


        } catch (error) {

            console.error(
                "Word to PDF error:",
                error
            );


            setMessage(
                "❌ Conversion failed: " +
                error.message
            );

            setDownloadReady(false);
        }
    };


    // ==========================================
    // UI
    // ==========================================

    return (

        <div className="featureContainer">

            <h2>
                📝 Word to PDF
            </h2>


            <p>
                Convert your Word document into a PDF document.
            </p>


            {/* =================================
                SELECT WORD FILE
            ================================= */}

            <div className="formGroup">

                <label>
                    📝 Select Word File
                </label>


                <select
                    value={selectedWord}
                    onChange={(e) =>
                        setSelectedWord(
                            e.target.value
                        )
                    }
                >

                    <option value="">
                        -- Select Word File --
                    </option>


                    {wordFiles.map((file) => (

                        <option
                            key={file.id}
                            value={file.id}
                        >
                            {file.fileName ||
                                file.file_name}
                        </option>

                    ))}

                </select>


                {/* No Word files message */}

                {wordFiles.length === 0 && (

                    <p
                        style={{
                            color: "#777",
                            marginTop: "8px"
                        }}
                    >
                        ⚠️ No Word files uploaded yet.
                    </p>

                )}

            </div>


            {/* =================================
                OUTPUT PDF NAME
            ================================= */}

            <div className="formGroup">

                <label>
                    📄 Output PDF Name
                </label>


                <input
                    type="text"
                    value={outputFileName}
                    onChange={(e) =>
                        setOutputFileName(
                            e.target.value
                        )
                    }
                    placeholder="converted.pdf"
                />

            </div>


            {/* =================================
                CONVERT BUTTON
            ================================= */}

            <button
                type="button"
                className="primaryButton"
                onClick={handleConvert}
            >
                📝 Convert to PDF
            </button>


            {/* =================================
                MESSAGE
            ================================= */}

            {message && (

                <div className="successMessage">

                    {message}


                    {/* =================================
                        DOWNLOAD BUTTON
                    ================================= */}

                    {downloadReady && (

                        <div
                            style={{
                                marginTop: "15px"
                            }}
                        >

                            <a
                                href={
                                    `http://localhost:8080/api/pdfs/download-pdf/${encodeURIComponent(
                                        outputFileName.trim()
                                    )}`
                                }
                                download
                                className="downloadButton"
                            >
                                ⬇️ Download PDF
                            </a>

                        </div>

                    )}

                </div>

            )}

        </div>
    );
}


export default WordToPdf;