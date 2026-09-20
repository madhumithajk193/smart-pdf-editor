import { useState } from "react";

function ImageToPdf({ onConversionSuccess }) {

    const [images, setImages] = useState([]);
    const [outputFileName, setOutputFileName] =
        useState("images_converted.pdf");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [downloadReady, setDownloadReady] = useState(false);


    const handleImageChange = (e) => {

        const selectedFiles =
            Array.from(e.target.files);

        setImages(selectedFiles);

        setMessage("");
        setError("");
        setDownloadReady(false);
    };


    const convertImagesToPdf = async () => {

        setMessage("");
        setError("");
        setDownloadReady(false);

        if (images.length === 0) {

            setError("Please select at least one image.");

            return;
        }

        if (!outputFileName.trim()) {

            setError("Please enter an output file name.");

            return;
        }

        setLoading(true);

        try {

            const formData = new FormData();

            images.forEach((image) => {

                formData.append("images", image);

            });

            formData.append(
                "outputFileName",
                outputFileName
            );


            const response = await fetch(
                "http://localhost:8080/api/pdfs/images-to-pdf",
                {
                    method: "POST",
                    body: formData
                }
            );


            const result = await response.text();


            if (!response.ok) {

                throw new Error(result);

            }


            setMessage(result);

            setDownloadReady(true);


            if (onConversionSuccess) {

                onConversionSuccess();

            }


        } catch (err) {

            console.error(
                "Images to PDF error:",
                err
            );

            setError(
                err.message ||
                "Error converting images to PDF."
            );


        } finally {

            setLoading(false);

        }
    };


    return (

        <div className="featureContainer">

            <div className="featureHeader">

                <h1>
                    🖼️ Images to PDF
                </h1>

                <p>
                    Convert multiple images into a single PDF.
                </p>

            </div>


            {/* Image Selection */}

            <div className="formGroup">

                <label>
                    🖼️ Select Images
                </label>

                <input
                    type="file"
                    accept="image/*"
                    multiple
                    onChange={handleImageChange}
                />

            </div>


            {/* Selected Images */}

            {images.length > 0 && (

                <div
                    style={{
                        marginTop: "15px",
                        marginBottom: "15px"
                    }}
                >

                    <strong>
                        Selected Images:
                    </strong>

                    <ul>

                        {images.map((image, index) => (

                            <li key={index}>

                                {image.name}

                            </li>

                        ))}

                    </ul>

                </div>

            )}


            {/* Output File Name */}

            <div className="formGroup">

                <label>
                    💾 Output File Name
                </label>

                <input
                    type="text"
                    value={outputFileName}
                    onChange={(e) =>
                        setOutputFileName(e.target.value)
                    }
                    placeholder="images_converted.pdf"
                />

            </div>


            {/* Convert Button */}

            <button
                className="primaryButton"
                onClick={convertImagesToPdf}
                disabled={loading}
            >

                {loading
                    ? "📄 Creating PDF..."
                    : "📄 Convert Images to PDF"
                }

            </button>


            {/* Success */}

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
                                href={`http://localhost:8080/api/pdfs/download-file/${outputFileName}`}
                                download
                                className="downloadButton"
                            >
                                ⬇️ Download PDF
                            </a>

                        </div>

                    )}

                </div>

            )}


            {/* Error */}

            {error && (

                <div className="errorMessage">

                    ❌ {error}

                </div>

            )}

        </div>

    );

}

export default ImageToPdf;