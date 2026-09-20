import { useState } from "react";

function Permissions({ pdfList, onPermissionSuccess }) {

    const [selectedPdf, setSelectedPdf] = useState("");
    const [ownerPassword, setOwnerPassword] = useState("");
    const [userPassword, setUserPassword] = useState("");

    const [allowPrinting, setAllowPrinting] = useState(true);
    const [allowCopying, setAllowCopying] = useState(true);
    const [allowModification, setAllowModification] = useState(false);

    const [outputFileName, setOutputFileName] =
        useState("protected_permissions.pdf");

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [downloadReady, setDownloadReady] = useState(false);


    const handleProtect = async () => {

        setMessage("");
        setError("");
        setDownloadReady(false);

        if (!selectedPdf) {
            setError("Please select a PDF.");
            return;
        }

        if (!ownerPassword.trim()) {
            setError("Please enter an owner password.");
            return;
        }

        if (!outputFileName.trim()) {
            setError("Please enter an output file name.");
            return;
        }

        setLoading(true);

        try {

            const params = new URLSearchParams();

            params.append("ownerPassword", ownerPassword);
            params.append("userPassword", userPassword);

            params.append(
                "allowPrinting",
                allowPrinting
            );

            params.append(
                "allowCopying",
                allowCopying
            );

            params.append(
                "allowModification",
                allowModification
            );

            params.append(
                "outputFileName",
                outputFileName
            );


            const response = await fetch(
                `http://localhost:8080/api/pdfs/permissions/${selectedPdf}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },
                    body: params
                }
            );


            const result = await response.text();


            if (!response.ok) {
                throw new Error(result);
            }


            setMessage(result);
            setDownloadReady(true);

            if (onPermissionSuccess) {
                onPermissionSuccess();
            }


        } catch (err) {

            console.error(
                "Permission error:",
                err
            );

            setError(
                err.message ||
                "Error setting PDF permissions."
            );

        } finally {

            setLoading(false);

        }

    };


    return (

        <div className="featureContainer">

            <div className="featureHeader">

                <h1>
                    🔒 PDF Permissions
                </h1>

                <p>
                    Control what users can do with your PDF.
                </p>

            </div>


            {/* Select PDF */}

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


            {/* Owner Password */}

            <div className="formGroup">

                <label>
                    🔑 Owner Password
                </label>

                <input
                    type="password"
                    value={ownerPassword}
                    onChange={(e) =>
                        setOwnerPassword(e.target.value)
                    }
                    placeholder="Enter owner password"
                />

            </div>


            {/* User Password */}

            <div className="formGroup">

                <label>
                    🔐 User Password
                </label>

                <input
                    type="password"
                    value={userPassword}
                    onChange={(e) =>
                        setUserPassword(e.target.value)
                    }
                    placeholder="Optional user password"
                />

            </div>


            {/* Permissions */}

            <div className="formGroup">

                <label>
                    ⚙️ Permissions
                </label>


                <div className="permissionOption">

                    <label>

                        <input
                            type="checkbox"
                            checked={allowPrinting}
                            onChange={(e) =>
                                setAllowPrinting(
                                    e.target.checked
                                )
                            }
                        />

                        🖨️ Allow Printing

                    </label>

                </div>


                <div className="permissionOption">

                    <label>

                        <input
                            type="checkbox"
                            checked={allowCopying}
                            onChange={(e) =>
                                setAllowCopying(
                                    e.target.checked
                                )
                            }
                        />

                        📋 Allow Copying

                    </label>

                </div>


                <div className="permissionOption">

                    <label>

                        <input
                            type="checkbox"
                            checked={allowModification}
                            onChange={(e) =>
                                setAllowModification(
                                    e.target.checked
                                )
                            }
                        />

                        ✏️ Allow Modification

                    </label>

                </div>

            </div>


            {/* Output Filename */}

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
                    placeholder="protected_permissions.pdf"
                />

            </div>


            {/* Button */}

            <button
                className="primaryButton"
                onClick={handleProtect}
                disabled={loading}
            >

                {loading
                    ? "🔒 Applying Permissions..."
                    : "🔒 Protect PDF with Permissions"
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
                                ⬇️ Download Protected PDF
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

export default Permissions;