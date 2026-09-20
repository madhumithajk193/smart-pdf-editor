import { useState } from "react";

function ExcelToPdf({ onConversionSuccess }) {

    const [file, setFile] = useState(null);
    const [message, setMessage] = useState("");
    const [downloadUrl, setDownloadUrl] = useState("");

    const handleFileChange = (event) => {

        const selectedFile = event.target.files[0];

        if (!selectedFile) {
            setFile(null);
            return;
        }

        const fileName =
            selectedFile.name.toLowerCase();

        if (!fileName.endsWith(".xlsx")) {

            setMessage(
                "Please select an Excel .xlsx file"
            );

            setFile(null);
            return;
        }

        setFile(selectedFile);
        setMessage("");
        setDownloadUrl("");
    };


    const handleConvert = async () => {

        if (!file) {

            setMessage(
                "Please select an Excel file first."
            );

            return;
        }

        try {

            setMessage("Uploading Excel file...");
            setDownloadUrl("");


            // =========================================
            // 1. UPLOAD EXCEL FILE
            // =========================================

            const formData =
                new FormData();

            formData.append(
                "file",
                file
            );


            const uploadResponse =
                await fetch(
                    "http://localhost:8080/api/pdfs/upload",
                    {
                        method: "POST",
                        body: formData
                    }
                );


            if (!uploadResponse.ok) {

                const error =
                    await uploadResponse.text();

                throw new Error(error);
            }


            setMessage(
                "Excel uploaded. Converting..."
            );


            // =========================================
            // 2. CONVERT EXCEL TO PDF
            // =========================================

            const outputFileName =
                file.name
                    .replace(/\.[^/.]+$/, "")
                + ".pdf";


            const convertUrl =
                "http://localhost:8080/api/pdfs/excel-to-pdf"
                + "?inputFileName="
                + encodeURIComponent(file.name)
                + "&outputFileName="
                + encodeURIComponent(outputFileName);


            const convertResponse =
                await fetch(
                    convertUrl,
                    {
                        method: "POST"
                    }
                );


            if (!convertResponse.ok) {

                const error =
                    await convertResponse.text();

                throw new Error(error);
            }


            const outputPath =
                await convertResponse.text();


            // =========================================
            // 3. GET GENERATED FILE NAME
            // =========================================

            const generatedFileName =
                outputPath
                    .split(/[\\/]/)
                    .pop();


            // =========================================
            // 4. CREATE DOWNLOAD URL
            // =========================================

            const url =
                "http://localhost:8080/api/pdfs/download-excel-pdf/"
                + encodeURIComponent(
                    generatedFileName
                );


            setDownloadUrl(url);


            setMessage(
                "Excel converted to PDF successfully!"
            );


            // =========================================
            // 5. REFRESH PDF LIST
            // =========================================

            if (onConversionSuccess) {

                onConversionSuccess();
            }


        } catch (error) {

            console.error(
                "Excel to PDF error:",
                error
            );

            setMessage(
                "Conversion failed: " +
                error.message
            );
        }
    };


    return (

        <div className="uploadPage">

            <div className="uploadBox">

                <div className="uploadIcon">
                    📊
                </div>

                <h2>
                    Excel to PDF
                </h2>

                <p className="uploadDescription">
                    Convert your Excel spreadsheet
                    into a PDF document.
                </p>


                <div className="uploadArea">

                    <p>
                        Select an Excel file
                    </p>


                    <input
                        type="file"
                        accept=".xlsx"
                        onChange={handleFileChange}
                    />


                    {file && (

                        <p>
                            Selected file:{" "}
                            <b>
                                {file.name}
                            </b>
                        </p>

                    )}


                    <button
                        className="uploadButton"
                        onClick={handleConvert}
                    >
                        📄 Convert to PDF
                    </button>


                    {message && (

                        <p>
                            {message}
                        </p>

                    )}


                    {downloadUrl && (

                        <a
                            href={downloadUrl}
                            download
                            className="uploadButton"
                        >
                            ⬇️ Download PDF
                        </a>

                    )}

                </div>

            </div>

        </div>

    );
}

export default ExcelToPdf;