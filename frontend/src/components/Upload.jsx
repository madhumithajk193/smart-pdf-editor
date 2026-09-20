import { useState } from "react";

function Upload({ onUploadSuccess }) {

    const [file, setFile] = useState(null);
    const [message, setMessage] = useState("");

    // Select file
    const handleFileChange = (event) => {

        const selectedFile = event.target.files[0];

        if (!selectedFile) {
            setFile(null);
            return;
        }

        setFile(selectedFile);
        setMessage("");
    };


    // Upload file
    const handleUpload = async () => {

        if (!file) {
            setMessage("Please select a PDF or Word file");
            return;
        }


        // Allow PDF, DOC and DOCX
        const fileName = file.name.toLowerCase();

        const isPdf = fileName.endsWith(".pdf");
        const isDoc = fileName.endsWith(".doc");
        const isDocx = fileName.endsWith(".docx");


        if (!isPdf && !isDoc && !isDocx) {

            setMessage(
                "Please select a PDF or Word file (.pdf, .doc, .docx)"
            );

            return;
        }


        const formData = new FormData();

        formData.append("file", file);


        try {

            setMessage("Uploading...");


            const response = await fetch(
                "http://localhost:8080/api/pdfs/upload",
                {
                    method: "POST",
                    body: formData
                }
            );


            if (response.ok) {

                const data = await response.json();

                setMessage(
                    "File uploaded successfully!"
                );


                // Tell App.jsx about uploaded file
                if (onUploadSuccess) {
                    onUploadSuccess(data);
                }


                // Clear selected file
                setFile(null);


                // Clear file input
                document.querySelector(
                    'input[type="file"]'
                ).value = "";

            } else {

                const errorText =
                    await response.text();

                console.log(errorText);

                setMessage(
                    "Upload failed: " + errorText
                );
            }


        } catch (error) {

            console.error(
                "Upload error:",
                error
            );

            setMessage(
                "Cannot connect to backend"
            );
        }

    };


    return (

        <div className="uploadPage">

            <div className="uploadBox">

                {/* Icon */}

                <div className="uploadIcon">
                    📄
                </div>


                {/* Heading */}

                <h2>
                    Upload File
                </h2>


                {/* Description */}

                <p className="uploadDescription">
                    Upload PDF or Word documents to
                    Smart PDF Editor.
                </p>


                {/* Upload Area */}

                <div className="uploadArea">

                    <p>
                        Select a file from your computer
                    </p>


                    <input
                        type="file"
                        accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        onChange={handleFileChange}
                    />


                    {/* Selected File */}

                    {file && (

                        <p>
                            Selected file:{" "}
                            <b>{file.name}</b>
                        </p>

                    )}


                    {/* Upload Button */}

                    <button
                        className="uploadButton"
                        onClick={handleUpload}
                    >
                        📤 Upload File
                    </button>


                    {/* Supported formats */}

                    <div className="uploadHint">
                        Supported formats: PDF, DOC, DOCX
                    </div>

                </div>


                {/* Message */}

                {message && (

                    <p
                        className={
                            message.includes(
                                "successfully"
                            )
                                ? "uploadSuccess"
                                : "uploadError"
                        }
                    >
                        {message}
                    </p>

                )}

            </div>

        </div>

    );
}

export default Upload;