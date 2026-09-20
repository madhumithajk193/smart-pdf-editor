import { useState } from "react";

function EditPdf({ pdfList, onEditSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");

    const [text, setText] = useState("");

    const [pageNumber, setPageNumber] = useState(1);

    const [x, setX] = useState(100);

    const [y, setY] = useState(500);

    const [fontSize, setFontSize] = useState(20);

    const [loading, setLoading] = useState(false);

    const [message, setMessage] = useState("");

    const [downloadFile, setDownloadFile] = useState("");


    const handleAddText = async () => {

        if (!selectedPdf) {
            setMessage("Please select a PDF.");
            return;
        }

        if (!text.trim()) {
            setMessage("Please enter some text.");
            return;
        }

        setLoading(true);
        setMessage("");
        setDownloadFile("");

        try {

            const outputFileName =
                `edited_${Date.now()}.pdf`;

            const formData = new URLSearchParams();

            formData.append("text", text);
            formData.append("pageNumber", pageNumber);
            formData.append("x", x);
            formData.append("y", y);
            formData.append("fontSize", fontSize);
            formData.append("outputFileName", outputFileName);


            const response = await fetch(
                `http://localhost:8080/api/pdfs/add-text/${selectedPdf}`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body: formData
                }
            );


            const result = await response.text();


            if (!response.ok) {

                throw new Error(result);

            }


            setMessage(
                "✅ Text added successfully!"
            );

            setDownloadFile(outputFileName);

            if (onEditSuccess) {
                onEditSuccess();
            }


        } catch (error) {

            console.error(
                "Error editing PDF:",
                error
            );

            setMessage(
                "❌ Error: " + error.message
            );

        } finally {

            setLoading(false);

        }

    };


    return (

        <div className="editPdfContainer">

            <div className="editPdfHeader">

                <h2>
                    ✏️ Edit PDF
                </h2>

                <p>
                    Add text to any page of your PDF document.
                </p>

            </div>


            {/* PDF Selection */}

            <div className="editSection">

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


            {/* Text */}

            <div className="editSection">

                <label>
                    📝 Text
                </label>
                <textarea
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    placeholder="Enter the text you want to add..."
                    className="editTextBox"
                />

            </div>


            {/* Page */}

            <div className="editRow">

                <div className="editSection">

                    <label>
                        📄 Page Number
                    </label>

                    <input
                        type="number"
                        min="1"
                        value={pageNumber}
                        onChange={(e) =>
                            setPageNumber(e.target.value)
                        }
                    />

                </div>


                {/* Font Size */}

                <div className="editSection">

                    <label>
                        🔤 Font Size
                    </label>

                    <input
                        type="number"
                        min="1"
                        max="100"
                        value={fontSize}
                        onChange={(e) =>
                            setFontSize(e.target.value)
                        }
                    />

                </div>

            </div>


            {/* Position */}

            <div className="positionBox">

                <h3>
                    📍 Text Position
                </h3>

                <p>
                    PDF coordinates start from the
                    bottom-left corner of the page.
                </p>


                <div className="editRow">

                    <div className="editSection">

                        <label>
                            X Position
                        </label>

                        <input
                            type="number"
                            value={x}
                            onChange={(e) =>
                                setX(e.target.value)
                            }
                        />

                    </div>


                    <div className="editSection">

                        <label>
                            Y Position
                        </label>

                        <input
                            type="number"
                            value={y}
                            onChange={(e) =>
                                setY(e.target.value)
                            }
                        />

                    </div>

                </div>

            </div>


            {/* Add Text Button */}

            <button
                className="addTextButton"
                onClick={handleAddText}
                disabled={loading}
            >

                {loading
                    ? "⏳ Adding Text..."
                    : "✏️ Add Text to PDF"
                }

            </button>


            {/* Message */}

            {message && (

                <div className="editMessage">

                    {message}

                </div>

            )}


            {/* Download */}

            {downloadFile && (

                <a
                    className="downloadEditedButton"
                    href={
                        `http://localhost:8080/api/pdfs/download-merged/${encodeURIComponent(downloadFile)}`
                    }
                    target="_blank"
                    rel="noreferrer"
                >
                    ⬇️ Download Edited PDF
                </a>

            )}

        </div>

    );

}

export default EditPdf;