import React, { useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function PdfFormField({
                          pdfList = [],
                          onSuccess,
                          onBackHome
                      }) {

    // =====================================================
    // STATE
    // =====================================================

    const [selectedPdf, setSelectedPdf] = useState("");

    const [fieldName, setFieldName] =
        useState("Employee Name");

    const [fieldType, setFieldType] =
        useState("TEXT");

    const [pageNumber, setPageNumber] =
        useState(1);

    const [x, setX] =
        useState(100);

    const [y, setY] =
        useState(150);

    const [width, setWidth] =
        useState(200);

    const [height, setHeight] =
        useState(30);

    const [fieldValue, setFieldValue] =
        useState("");

    const [outputFileName, setOutputFileName] =
        useState("filled-form.pdf");

    const [loading, setLoading] =
        useState(false);

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");

    const [createdField, setCreatedField] =
        useState(null);

    const [resultFileName, setResultFileName] =
        useState("");

    // =====================================================
    // SIGNATURE STATE
    // =====================================================

    const [signatureImage, setSignatureImage] =
        useState(null);

    const [signatureFileName, setSignatureFileName] =
        useState("");

    // =====================================================
    // SIGNATURE UPLOAD
    // =====================================================

    const handleSignatureUpload = (e) => {

        const file =
            e.target.files?.[0];

        if (!file) {
            return;
        }

        // Check image
        if (!file.type.startsWith("image/")) {

            setError(
                "Please upload a signature image."
            );

            setSignatureImage(null);
            setSignatureFileName("");

            return;
        }

        setError("");

        setSignatureFileName(
            file.name
        );

        const reader =
            new FileReader();

        reader.onload = () => {

            setSignatureImage(
                reader.result
            );

        };

        reader.onerror = () => {

            setError(
                "Unable to read signature image."
            );

        };

        reader.readAsDataURL(file);
    };

    // =====================================================
    // REMOVE SIGNATURE
    // =====================================================

    const handleRemoveSignature = () => {

        setSignatureImage(null);
        setSignatureFileName("");

    };

    // =====================================================
    // CREATE FORM FIELD
    // =====================================================

    const handleCreateField = async () => {

        setMessage("");
        setError("");
        setCreatedField(null);

        // -------------------------------------------------
        // VALIDATION
        // -------------------------------------------------

        if (!selectedPdf) {

            setError(
                "Please select a PDF."
            );

            return;
        }

        if (!fieldName.trim()) {

            setError(
                "Please enter a field name."
            );

            return;
        }

        if (!fieldValue.trim()) {

            setError(
                "Please enter a field value."
            );

            return;
        }

        if (Number(pageNumber) < 1) {

            setError(
                "Page number must be at least 1."
            );

            return;
        }

        if (Number(width) <= 0 ||
            Number(height) <= 0) {

            setError(
                "Width and height must be greater than 0."
            );

            return;
        }

        setLoading(true);

        try {

            // -------------------------------------------------
            // PARAMETERS
            // -------------------------------------------------

            const params =
                new URLSearchParams();

            params.append(
                "fieldName",
                fieldName.trim()
            );

            params.append(
                "fieldType",
                fieldType
            );

            params.append(
                "pageNumber",
                Number(pageNumber)
            );

            params.append(
                "x",
                Number(x)
            );

            params.append(
                "y",
                Number(y)
            );

            params.append(
                "width",
                Number(width)
            );

            params.append(
                "height",
                Number(height)
            );

            const url =
                `${API_BASE_URL}/api/pdfs/form-fields/${selectedPdf}?${params.toString()}`;

            console.log(
                "Create Form Field URL:",
                url
            );

            const response =
                await fetch(url, {
                    method: "POST"
                });

            const text =
                await response.text();

            console.log(
                "Create field response:",
                text
            );

            let result = null;

            try {

                result =
                    JSON.parse(text);

            } catch {

                result = null;

            }

            if (!response.ok) {

                throw new Error(
                    result?.message ||
                    result?.error ||
                    text ||
                    "Failed to create form field."
                );

            }

            setCreatedField(
                result
            );

            setMessage(
                "Form field created successfully!"
            );

        } catch (err) {

            console.error(
                "Create form field error:",
                err
            );

            setError(
                err.message ||
                "Failed to create form field."
            );

        } finally {

            setLoading(false);

        }
    };

    // =====================================================
    // FILL PDF
    // =====================================================

    const handleFillPdf = async () => {

        setMessage("");
        setError("");
        setResultFileName("");

        // -------------------------------------------------
        // VALIDATION
        // -------------------------------------------------

        if (!selectedPdf) {

            setError(
                "Please select a PDF."
            );

            return;
        }

        if (!fieldValue.trim()) {

            setError(
                "Please enter the employee name/value."
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

            // =================================================
            // CREATE FIELD OBJECT
            // =================================================

            const field = {

                id:
                    createdField?.id || 0,

                fieldName:
                    fieldName.trim(),

                fieldType:
                fieldType,

                pageNumber:
                    Number(pageNumber),

                x:
                    Number(x),

                y:
                    Number(y),

                width:
                    Number(width),

                height:
                    Number(height),

                fieldValue:
                    fieldValue.trim()
            };

            console.log(
                "Field being sent:",
                field
            );

            // =================================================
            // OUTPUT FILE
            // =================================================

            let finalOutputFileName =
                outputFileName.trim();

            if (
                !finalOutputFileName
                    .toLowerCase()
                    .endsWith(".pdf")
            ) {

                finalOutputFileName +=
                    ".pdf";

            }

            // =================================================
            // REQUEST DATA
            //
            // The backend must support:
            // {
            //   fields: [...],
            //   signatureImage: "data:image/..."
            // }
            // =================================================

            const requestData = {

                fields: [
                    field
                ],

                signatureImage:
                    signatureImage || null

            };

            console.log(
                "Fill PDF request:",
                requestData
            );

            // =================================================
            // URL
            // =================================================

            const url =
                `${API_BASE_URL}/api/pdfs/form-fields/fill/${selectedPdf}?outputFileName=${encodeURIComponent(
                    finalOutputFileName
                )}`;

            console.log(
                "Fill PDF URL:",
                url
            );

            // =================================================
            // SEND REQUEST
            // =================================================

            const response =
                await fetch(url, {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json"

                    },

                    body:
                        JSON.stringify(
                            requestData
                        )

                });

            const text =
                await response.text();

            console.log(
                "Fill PDF response:",
                text
            );

            let result = null;

            try {

                result =
                    JSON.parse(text);

            } catch {

                result = null;

            }

            if (!response.ok) {

                throw new Error(
                    result?.message ||
                    result?.error ||
                    text ||
                    "PDF filling failed."
                );

            }

            // =================================================
            // SUCCESS
            // =================================================

            const generatedFileName =
                result?.fileName ||
                finalOutputFileName;

            setResultFileName(
                generatedFileName
            );

            setMessage(
                result?.message ||
                "PDF filled successfully!"
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
                "Fill PDF error:",
                err
            );

            setError(
                err.message ||
                "PDF filling failed."
            );

        } finally {

            setLoading(false);

        }
    };

    // =====================================================
    // DOWNLOAD FILLED PDF
    // =====================================================

    const handleDownload = () => {

        if (!resultFileName) {

            setError(
                "No generated PDF available."
            );

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

        document.body.appendChild(
            link
        );

        link.click();

        document.body.removeChild(
            link
        );
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

            {/* =================================================
                HEADER
            ================================================= */}

            <div className="featureHeader">

                <h1>
                    📝 PDF Form Filler
                </h1>

                <p>
                    Fill employee information and add a
                    signature to your PDF form.
                </p>

            </div>


            {/* =================================================
                MAIN CARD
            ================================================= */}

            <div className="featureCard">

                <h2>
                    Employee Form
                </h2>

                <p>
                    Select your PDF and enter the employee
                    information that should appear in the form.
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
                        onChange={(e) => {

                            setSelectedPdf(
                                e.target.value
                            );

                            setMessage("");
                            setError("");
                            setCreatedField(null);
                            setResultFileName("");

                        }}
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


                {/* =================================================
                    FIELD NAME
                ================================================= */}

                <div className="formGroup">

                    <label>
                        Field Name
                    </label>

                    <input
                        type="text"
                        value={fieldName}
                        onChange={(e) =>
                            setFieldName(
                                e.target.value
                            )
                        }
                        placeholder="Employee Name"
                    />

                </div>


                {/* =================================================
                    FIELD TYPE
                ================================================= */}

                <div className="formGroup">

                    <label>
                        Field Type
                    </label>

                    <select
                        value={fieldType}
                        onChange={(e) =>
                            setFieldType(
                                e.target.value
                            )
                        }
                    >

                        <option value="TEXT">
                            Text
                        </option>

                        <option value="NUMBER">
                            Number
                        </option>

                        <option value="DATE">
                            Date
                        </option>

                        <option value="EMAIL">
                            Email
                        </option>

                    </select>

                </div>


                {/* =================================================
                    FIELD VALUE
                ================================================= */}

                <div className="formGroup">

                    <label>
                        👤 Employee Name / Field Value
                    </label>

                    <input
                        type="text"
                        value={fieldValue}
                        onChange={(e) =>
                            setFieldValue(
                                e.target.value
                            )
                        }
                        placeholder="Enter employee name"
                    />

                </div>


                {/* =================================================
                    PAGE NUMBER
                ================================================= */}

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


                {/* =================================================
                    POSITION
                ================================================= */}

                <div className="formRow">

                    <div className="formGroup">

                        <label>
                            X Position
                        </label>

                        <input
                            type="number"
                            value={x}
                            onChange={(e) =>
                                setX(
                                    Number(e.target.value)
                                )
                            }
                        />

                    </div>


                    <div className="formGroup">

                        <label>
                            Y Position
                        </label>

                        <input
                            type="number"
                            value={y}
                            onChange={(e) =>
                                setY(
                                    Number(e.target.value)
                                )
                            }
                        />

                    </div>

                </div>


                {/* =================================================
                    SIZE
                ================================================= */}

                <div className="formRow">

                    <div className="formGroup">

                        <label>
                            Width
                        </label>

                        <input
                            type="number"
                            min="1"
                            value={width}
                            onChange={(e) =>
                                setWidth(
                                    Number(e.target.value)
                                )
                            }
                        />

                    </div>


                    <div className="formGroup">

                        <label>
                            Height
                        </label>

                        <input
                            type="number"
                            min="1"
                            value={height}
                            onChange={(e) =>
                                setHeight(
                                    Number(e.target.value)
                                )
                            }
                        />

                    </div>

                </div>


                {/* =================================================
                    SIGNATURE
                ================================================= */}

                <div className="formGroup">

                    <label>
                        ✍️ Employee Signature
                    </label>

                    <input
                        type="file"
                        accept="image/png,image/jpeg,image/jpg,image/webp"
                        onChange={
                            handleSignatureUpload
                        }
                    />

                    <p
                        style={{
                            fontSize: "13px",
                            marginTop: "6px",
                            color: "#777"
                        }}
                    >
                        Upload a PNG/JPG image of the
                        employee signature.
                    </p>

                </div>


                {/* =================================================
                    SIGNATURE PREVIEW
                ================================================= */}

                {signatureImage && (

                    <div
                        className="signaturePreview"
                        style={{
                            marginTop: "15px",
                            padding: "15px",
                            border: "1px solid #ddd",
                            borderRadius: "10px",
                            background: "#fafafa"
                        }}
                    >

                        <strong>
                            ✍️ Signature Preview
                        </strong>

                        <div
                            style={{
                                marginTop: "10px",
                                background: "#fff",
                                padding: "15px",
                                borderRadius: "8px",
                                display: "inline-block"
                            }}
                        >

                            <img
                                src={signatureImage}
                                alt="Employee Signature"
                                style={{
                                    maxWidth: "300px",
                                    maxHeight: "120px",
                                    display: "block",
                                    objectFit: "contain"
                                }}
                            />

                        </div>

                        {signatureFileName && (

                            <p
                                style={{
                                    fontSize: "13px",
                                    marginTop: "8px"
                                }}
                            >
                                📄 {signatureFileName}
                            </p>

                        )}

                        <button
                            type="button"
                            onClick={
                                handleRemoveSignature
                            }
                            style={{
                                marginTop: "8px",
                                padding: "7px 12px",
                                border: "none",
                                borderRadius: "6px",
                                cursor: "pointer"
                            }}
                        >
                            ❌ Remove Signature
                        </button>

                    </div>

                )}


                {/* =================================================
                    OUTPUT FILE
                ================================================= */}

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
                        placeholder="filled-form.pdf"
                    />

                </div>


                {/* =================================================
                    ERROR
                ================================================= */}

                {error && (

                    <div
                        className="errorMessage"
                        style={{
                            marginTop: "15px"
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
                        className="successMessage"
                        style={{
                            marginTop: "15px"
                        }}
                    >

                        ✅ {message}

                    </div>

                )}


                {/* =================================================
                    CREATE FIELD
                ================================================= */}

                <button
                    className="primaryButton"
                    onClick={
                        handleCreateField
                    }
                    disabled={loading}
                    style={{
                        marginTop: "15px"
                    }}
                >

                    {loading
                        ? "⏳ Processing..."
                        : "➕ Create Form Field"}

                </button>


                {/* =================================================
                    CREATED FIELD
                ================================================= */}

                {createdField && (

                    <div
                        className="successMessage"
                        style={{
                            marginTop: "15px"
                        }}
                    >

                        <strong>
                            ✅ Field Created
                        </strong>

                        <p>
                            Field:{" "}
                            {createdField.fieldName ||
                                fieldName}
                        </p>

                        <p>
                            Position: (
                            {createdField.x ??
                                x},{" "}
                            {createdField.y ??
                                y}
                            )
                        </p>

                    </div>

                )}


                {/* =================================================
                    FILL PDF
                ================================================= */}

                <button
                    className="primaryButton"
                    onClick={
                        handleFillPdf
                    }
                    disabled={loading}
                    style={{
                        marginTop: "10px"
                    }}
                >

                    {loading
                        ? "⏳ Filling PDF..."
                        : "📝 Fill PDF"}

                </button>


                {/* =================================================
                    DOWNLOAD
                ================================================= */}

                {resultFileName && (

                    <div
                        className="resultActions"
                        style={{
                            marginTop: "20px"
                        }}
                    >

                        <button
                            className="downloadButton"
                            onClick={
                                handleDownload
                            }
                        >

                            ⬇️ Download Filled PDF

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

export default PdfFormField;