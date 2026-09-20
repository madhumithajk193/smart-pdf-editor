import React, { useState } from "react";

const API_BASE = "http://localhost:8080";

function PdfProperties({ pdfList = [] }) {

    const [selectedPdfId, setSelectedPdfId] = useState("");
    const [properties, setProperties] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // ============================================================
    // GET PDF PROPERTIES
    // ============================================================

    const getProperties = async () => {

        if (!selectedPdfId) {
            setError("Please select a PDF.");
            setProperties(null);
            return;
        }

        setLoading(true);
        setError("");
        setProperties(null);

        try {

            const response = await fetch(
                `${API_BASE}/api/pdf-properties/${selectedPdfId}`
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Failed to load PDF properties."
                );
            }

            setProperties(data);

        } catch (err) {

            console.error(err);

            setError(
                err.message ||
                "Unable to load PDF properties."
            );

        } finally {

            setLoading(false);
        }
    };


    // ============================================================
    // FORMAT FILE SIZE
    // ============================================================

    const formatFileSize = (bytes) => {

        if (!bytes || bytes <= 0) {
            return "0 Bytes";
        }

        const units = [
            "Bytes",
            "KB",
            "MB",
            "GB"
        ];

        const index =
            Math.floor(
                Math.log(bytes) /
                Math.log(1024)
            );

        return (
            (bytes / Math.pow(1024, index))
                .toFixed(2)
            + " "
            + units[index]
        );
    };


    // ============================================================
    // PDF NAME
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
    // PROPERTY ROW
    // ============================================================

    const PropertyRow = ({
                             label,
                             value
                         }) => {

        return (
            <div className="propertyRow">

                <span className="propertyLabel">
                    {label}
                </span>

                <span className="propertyValue">
                    {value ||
                        "Not available"}
                </span>

            </div>
        );
    };


    // ============================================================
    // RENDER
    // ============================================================

    return (
        <div className="featureContainer">

            {/* ================================================== */}
            {/* HEADER */}
            {/* ================================================== */}

            <div className="featureHeader">

                <div className="featureIcon">
                    📄
                </div>

                <div>

                    <h1>
                        PDF Properties
                    </h1>

                    <p>
                        View information and metadata
                        about your PDF.
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
                        className="pdfSelect"
                        value={selectedPdfId}
                        onChange={(e) => {

                            setSelectedPdfId(
                                e.target.value
                            );

                            setProperties(null);
                            setError("");

                        }}
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
                {/* BUTTON */}
                {/* ================================================== */}

                <button
                    className="primaryButton"
                    onClick={getProperties}
                    disabled={
                        loading ||
                        !selectedPdfId
                    }
                >

                    {loading
                        ? "Loading..."
                        : "🔍 View PDF Properties"
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
            {/* PROPERTIES */}
            {/* ================================================== */}

            {properties && (

                <div className="featureCard">

                    <h2>
                        PDF Information
                    </h2>


                    {/* ================================================== */}
                    {/* BASIC INFORMATION */}
                    {/* ================================================== */}

                    <div className="propertiesSection">

                        <h3>
                            📋 File Information
                        </h3>

                        <PropertyRow
                            label="File Name"
                            value={
                                properties.fileName
                            }
                        />

                        <PropertyRow
                            label="File Size"
                            value={
                                formatFileSize(
                                    properties.fileSize
                                )
                            }
                        />

                        <PropertyRow
                            label="Number of Pages"
                            value={
                                properties.pageCount
                            }
                        />

                        <PropertyRow
                            label="PDF Version"
                            value={
                                properties.pdfVersion
                            }
                        />

                    </div>


                    {/* ================================================== */}
                    {/* DOCUMENT METADATA */}
                    {/* ================================================== */}

                    <div className="propertiesSection">

                        <h3>
                            📝 Document Metadata
                        </h3>

                        <PropertyRow
                            label="Title"
                            value={
                                properties.title
                            }
                        />

                        <PropertyRow
                            label="Author"
                            value={
                                properties.author
                            }
                        />

                        <PropertyRow
                            label="Subject"
                            value={
                                properties.subject
                            }
                        />

                        <PropertyRow
                            label="Keywords"
                            value={
                                properties.keywords
                            }
                        />

                    </div>


                    {/* ================================================== */}
                    {/* PDF SOFTWARE INFORMATION */}
                    {/* ================================================== */}

                    <div className="propertiesSection">

                        <h3>
                            ⚙️ PDF Software
                        </h3>

                        <PropertyRow
                            label="Creator"
                            value={
                                properties.creator
                            }
                        />

                        <PropertyRow
                            label="Producer"
                            value={
                                properties.producer
                            }
                        />

                    </div>


                    {/* ================================================== */}
                    {/* DATES */}
                    {/* ================================================== */}

                    <div className="propertiesSection">

                        <h3>
                            📅 Dates
                        </h3>

                        <PropertyRow
                            label="Creation Date"
                            value={
                                properties.creationDate
                            }
                        />

                        <PropertyRow
                            label="Modification Date"
                            value={
                                properties.modificationDate
                            }
                        />

                    </div>

                </div>

            )}

        </div>
    );
}

export default PdfProperties;