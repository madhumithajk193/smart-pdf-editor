import React, { useState } from "react";

const API_BASE = "http://localhost:8080";

function PdfSignatureVerification({ pdfList = [] }) {

    const [selectedPdfId, setSelectedPdfId] = useState("");
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // ============================================================
    // VERIFY SIGNATURE
    // ============================================================

    const verifySignature = async () => {

        if (!selectedPdfId) {
            setError("Please select a PDF.");
            setResult(null);
            return;
        }

        setLoading(true);
        setError("");
        setResult(null);

        try {

            const response = await fetch(
                `${API_BASE}/api/signature/verify/${selectedPdfId}`
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to verify PDF signature."
                );
            }

            setResult(data);

        } catch (err) {

            console.error(err);

            setError(
                err.message ||
                "Unable to verify the PDF signature."
            );

        } finally {

            setLoading(false);
        }
    };


    // ============================================================
    // GET PDF NAME
    // ============================================================

    const getPdfName = (pdf) => {

        return (
            pdf.fileName ||
            pdf.filename ||
            pdf.name ||
            `PDF ${pdf.id}`
        );
    };


    // ============================================================
    // RENDER
    // ============================================================

    return (
        <div className="featureContainer">

            {/* ================================================== */}
            {/* TITLE */}
            {/* ================================================== */}

            <div className="featureHeader">

                <div className="featureIcon">
                    🔐
                </div>

                <div>
                    <h1>
                        Digital Signature Verification
                    </h1>

                    <p>
                        Check whether a PDF contains a digital signature.
                    </p>
                </div>

            </div>


            {/* ================================================== */}
            {/* PDF SELECTION */}
            {/* ================================================== */}

            <div className="featureCard">

                <h2>
                    Select PDF
                </h2>

                {pdfList.length === 0 ? (

                    <div className="emptyMessage">
                        No uploaded PDFs available.
                    </div>

                ) : (

                    <select
                        value={selectedPdfId}
                        onChange={(e) => {
                            setSelectedPdfId(e.target.value);
                            setResult(null);
                            setError("");
                        }}
                        className="pdfSelect"
                    >

                        <option value="">
                            Select a PDF
                        </option>

                        {pdfList.map((pdf) => (

                            <option
                                key={pdf.id}
                                value={pdf.id}
                            >
                                {getPdfName(pdf)}
                            </option>

                        ))}

                    </select>
                )}


                {/* ================================================== */}
                {/* VERIFY BUTTON */}
                {/* ================================================== */}

                <button
                    className="primaryButton"
                    onClick={verifySignature}
                    disabled={
                        loading ||
                        !selectedPdfId
                    }
                >

                    {loading
                        ? "Verifying..."
                        : "🔍 Verify Digital Signature"
                    }

                </button>


                {/* ================================================== */}
                {/* ERROR */}
                {/* ================================================== */}

                {error && (

                    <div className="errorMessage">
                        ❌ {error}
                    </div>

                )}

            </div>


            {/* ================================================== */}
            {/* RESULT */}
            {/* ================================================== */}

            {result && (

                <div className="featureCard">

                    <h2>
                        Verification Result
                    </h2>


                    {/* ========================================== */}
                    {/* SIGNED / UNSIGNED */}
                    {/* ========================================== */}

                    <div
                        className={
                            result.signed
                                ? "signatureStatus signed"
                                : "signatureStatus unsigned"
                        }
                    >

                        <span className="statusIcon">

                            {result.signed
                                ? "✅"
                                : "❌"
                            }

                        </span>

                        <div>

                            <strong>
                                {result.signed
                                    ? "Digital Signature Found"
                                    : "No Digital Signature"
                                }
                            </strong>

                            <p>
                                {result.message}
                            </p>

                        </div>

                    </div>


                    {/* ========================================== */}
                    {/* PDF INFORMATION */}
                    {/* ========================================== */}

                    <div className="signatureInfo">

                        <div className="infoRow">

                            <span>
                                PDF
                            </span>

                            <strong>
                                {result.fileName}
                            </strong>

                        </div>


                        <div className="infoRow">

                            <span>
                                Signature Count
                            </span>

                            <strong>
                                {result.signatureCount ?? 0}
                            </strong>

                        </div>

                    </div>


                    {/* ================================================== */}
                    {/* SIGNATURE DETAILS */}
                    {/* ================================================== */}

                    {result.signatures &&
                        result.signatures.length > 0 && (

                            <div className="signatureDetails">

                                <h3>
                                    Signature Details
                                </h3>


                                {result.signatures.map(
                                    (signature, index) => (

                                        <div
                                            className="signatureBox"
                                            key={index}
                                        >

                                            <h4>
                                                Signature {index + 1}
                                            </h4>


                                            <div className="infoRow">

                                        <span>
                                            Signer Name
                                        </span>

                                                <strong>
                                                    {signature.name ||
                                                        "Not provided"}
                                                </strong>

                                            </div>


                                            <div className="infoRow">

                                        <span>
                                            Location
                                        </span>

                                                <strong>
                                                    {signature.location ||
                                                        "Not provided"}
                                                </strong>

                                            </div>


                                            <div className="infoRow">

                                        <span>
                                            Reason
                                        </span>

                                                <strong>
                                                    {signature.reason ||
                                                        "Not provided"}
                                                </strong>

                                            </div>


                                            <div className="infoRow">

                                        <span>
                                            Contact
                                        </span>

                                                <strong>
                                                    {signature.contactInfo ||
                                                        "Not provided"}
                                                </strong>

                                            </div>


                                            <div className="infoRow">

                                        <span>
                                            Signature Date
                                        </span>

                                                <strong>
                                                    {signature.signDate ||
                                                        "Not provided"}
                                                </strong>

                                            </div>


                                            <div className="infoRow">

                                        <span>
                                            SubFilter
                                        </span>

                                                <strong>
                                                    {signature.subFilter ||
                                                        "Not provided"}
                                                </strong>

                                            </div>


                                            <div className="infoRow">

                                        <span>
                                            Status
                                        </span>

                                                <strong>
                                                    {signature.status ||
                                                        "Unknown"}
                                                </strong>

                                            </div>


                                            <div className="infoRow">

                                        <span>
                                            Cryptographic Verification
                                        </span>

                                                <strong>
                                                    {signature.cryptographicVerification ||
                                                        "Not verified"}
                                                </strong>

                                            </div>

                                        </div>

                                    ))}

                            </div>

                        )}


                    {/* ================================================== */}
                    {/* VERIFICATION NOTE */}
                    {/* ================================================== */}

                    {result.verificationNote && (

                        <div className="verificationNote">

                            ℹ️ {result.verificationNote}

                        </div>

                    )}

                </div>

            )}

        </div>
    );
}

export default PdfSignatureVerification;