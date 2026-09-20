import React, { useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function PdfResize({ pdfList = [] }) {
    const [selectedPdf, setSelectedPdf] = useState("");
    const [pageSize, setPageSize] = useState("A4");
    const [orientation, setOrientation] = useState("PORTRAIT");
    const [applyTo, setApplyTo] = useState("ALL");
    const [pageNumber, setPageNumber] = useState("");
    const [outputFileName, setOutputFileName] = useState("");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    // ============================================================
    // RESIZE PDF
    // ============================================================

    const handleResize = async () => {
        setMessage("");
        setError("");

        if (!selectedPdf) {
            setError("Please select a PDF.");
            return;
        }

        if (
            applyTo === "CURRENT" &&
            (!pageNumber || Number(pageNumber) < 1)
        ) {
            setError("Please enter a valid page number.");
            return;
        }

        try {
            setLoading(true);

            const params = new URLSearchParams();

            params.append("pageSize", pageSize);
            params.append("orientation", orientation);
            params.append("applyTo", applyTo);

            if (applyTo === "CURRENT") {
                params.append("pageNumber", pageNumber);
            }

            if (outputFileName.trim()) {
                params.append(
                    "outputFileName",
                    outputFileName.trim()
                );
            }

            const response = await fetch(
                `${API_BASE_URL}/api/pdfs/resize/${selectedPdf}?${params.toString()}`,
                {
                    method: "POST"
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.error ||
                    data.message ||
                    "Failed to resize PDF."
                );
            }

            setMessage(
                data.message ||
                "PDF resized successfully!"
            );

            // ====================================================
            // DOWNLOAD
            // ====================================================

            if (data.downloadUrl) {
                const downloadUrl =
                    data.downloadUrl.startsWith("http")
                        ? data.downloadUrl
                        : `${API_BASE_URL}${data.downloadUrl}`;

                const link =
                    document.createElement("a");

                link.href = downloadUrl;

                link.download =
                    data.fileName ||
                    "resized.pdf";

                document.body.appendChild(link);

                link.click();

                document.body.removeChild(link);
            }

        } catch (err) {
            console.error(err);

            setError(
                err.message ||
                "Something went wrong."
            );

        } finally {
            setLoading(false);
        }
    };


    // ============================================================
    // GET PDF NAME
    // ============================================================

    const getPdfName = (pdf) => {
        if (typeof pdf === "string") {
            return pdf;
        }

        return (
            pdf.fileName ||
            pdf.filename ||
            pdf.name ||
            `PDF ${pdf.id}`
        );
    };


    // ============================================================
    // GET PDF ID
    // ============================================================

    const getPdfId = (pdf) => {
        if (typeof pdf === "number") {
            return pdf;
        }

        if (typeof pdf === "string") {
            return pdf;
        }

        return pdf.id;
    };


    // ============================================================
    // PAGE SIZE PREVIEW
    // ============================================================

    const getPreviewDimensions = () => {
        let width = 180;
        let height = 255;

        if (pageSize === "A3") {
            width = 190;
            height = 270;
        }

        if (pageSize === "A4") {
            width = 180;
            height = 255;
        }

        if (pageSize === "A5") {
            width = 160;
            height = 225;
        }

        if (pageSize === "LETTER") {
            width = 180;
            height = 235;
        }

        if (pageSize === "LEGAL") {
            width = 175;
            height = 285;
        }

        if (orientation === "LANDSCAPE") {
            const temp = width;
            width = height;
            height = temp;
        }

        return {
            width,
            height
        };
    };

    const preview =
        getPreviewDimensions();


    return (
        <div
            style={{
                width: "100%",
                maxWidth: "900px",
                margin: "0 auto",
                padding: "30px",
                boxSizing: "border-box",
                color: "#111827"
            }}
        >

            {/* ================================================= */}
            {/* HEADER */}
            {/* ================================================= */}

            <div
                style={{
                    textAlign: "center",
                    marginBottom: "35px"
                }}
            >
                <h1
                    style={{
                        margin: "0 0 10px 0",
                        color: "#111827",
                        fontSize: "30px"
                    }}
                >
                    📐 Resize PDF Pages
                </h1>

                <p
                    style={{
                        margin: 0,
                        color: "#6b7280",
                        fontSize: "15px"
                    }}
                >
                    Change your PDF page size and orientation.
                </p>
            </div>


            {/* ================================================= */}
            {/* SELECT PDF */}
            {/* ================================================= */}

            <div
                style={{
                    marginBottom: "28px"
                }}
            >
                <label
                    style={{
                        display: "block",
                        marginBottom: "9px",
                        fontWeight: "600",
                        color: "#111827"
                    }}
                >
                    Select PDF
                </label>

                <select
                    value={selectedPdf}
                    onChange={(e) =>
                        setSelectedPdf(e.target.value)
                    }
                    style={{
                        width: "100%",
                        padding: "12px 14px",
                        borderRadius: "8px",
                        border: "1px solid #cbd5e1",
                        fontSize: "15px",
                        color: "#111827",
                        backgroundColor: "#ffffff",
                        boxSizing: "border-box",
                        outline: "none"
                    }}
                >
                    <option
                        value=""
                        style={{
                            color: "#111827"
                        }}
                    >
                        -- Select a PDF --
                    </option>

                    {pdfList.map((pdf, index) => (
                        <option
                            key={
                                getPdfId(pdf) ||
                                index
                            }
                            value={getPdfId(pdf)}
                            style={{
                                color: "#111827",
                                backgroundColor: "#ffffff"
                            }}
                        >
                            {getPdfName(pdf)}
                        </option>
                    ))}
                </select>
            </div>


            {/* ================================================= */}
            {/* PAGE SIZE */}
            {/* ================================================= */}

            <div
                style={{
                    marginBottom: "28px"
                }}
            >
                <h3
                    style={{
                        textAlign: "center",
                        marginBottom: "14px",
                        color: "#111827"
                    }}
                >
                    Page Size
                </h3>

                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns:
                            "repeat(5, minmax(0, 1fr))",
                        gap: "10px"
                    }}
                >
                    {[
                        "A3",
                        "A4",
                        "A5",
                        "LETTER",
                        "LEGAL"
                    ].map((size) => {

                        const selected =
                            pageSize === size;

                        return (
                            <button
                                key={size}
                                type="button"
                                onClick={() =>
                                    setPageSize(size)
                                }
                                style={{
                                    padding: "14px 10px",
                                    borderRadius: "8px",
                                    border: selected
                                        ? "2px solid #2563eb"
                                        : "1px solid #cbd5e1",
                                    backgroundColor:
                                        selected
                                            ? "#eff6ff"
                                            : "#ffffff",
                                    color: "#111827",
                                    cursor: "pointer",
                                    fontWeight: "600",
                                    fontSize: "14px"
                                }}
                            >
                                {size}
                            </button>
                        );
                    })}
                </div>
            </div>


            {/* ================================================= */}
            {/* ORIENTATION */}
            {/* ================================================= */}

            <div
                style={{
                    marginBottom: "28px"
                }}
            >
                <h3
                    style={{
                        textAlign: "center",
                        marginBottom: "14px",
                        color: "#111827"
                    }}
                >
                    Orientation
                </h3>

                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns:
                            "1fr 1fr",
                        gap: "12px"
                    }}
                >

                    <button
                        type="button"
                        onClick={() =>
                            setOrientation("PORTRAIT")
                        }
                        style={{
                            padding: "15px",
                            borderRadius: "8px",
                            border:
                                orientation === "PORTRAIT"
                                    ? "2px solid #2563eb"
                                    : "1px solid #cbd5e1",
                            backgroundColor:
                                orientation === "PORTRAIT"
                                    ? "#eff6ff"
                                    : "#ffffff",
                            color: "#111827",
                            cursor: "pointer",
                            fontWeight: "600",
                            fontSize: "15px"
                        }}
                    >
                        📄 Portrait
                    </button>


                    <button
                        type="button"
                        onClick={() =>
                            setOrientation("LANDSCAPE")
                        }
                        style={{
                            padding: "15px",
                            borderRadius: "8px",
                            border:
                                orientation === "LANDSCAPE"
                                    ? "2px solid #2563eb"
                                    : "1px solid #cbd5e1",
                            backgroundColor:
                                orientation === "LANDSCAPE"
                                    ? "#eff6ff"
                                    : "#ffffff",
                            color: "#111827",
                            cursor: "pointer",
                            fontWeight: "600",
                            fontSize: "15px"
                        }}
                    >
                        🖼️ Landscape
                    </button>

                </div>
            </div>


            {/* ================================================= */}
            {/* APPLY TO */}
            {/* ================================================= */}

            <div
                style={{
                    marginBottom: "28px"
                }}
            >
                <h3
                    style={{
                        textAlign: "center",
                        marginBottom: "14px",
                        color: "#111827"
                    }}
                >
                    Apply Resize To
                </h3>

                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns:
                            "1fr 1fr",
                        gap: "12px"
                    }}
                >

                    <button
                        type="button"
                        onClick={() =>
                            setApplyTo("ALL")
                        }
                        style={{
                            padding: "15px",
                            borderRadius: "8px",
                            border:
                                applyTo === "ALL"
                                    ? "2px solid #2563eb"
                                    : "1px solid #cbd5e1",
                            backgroundColor:
                                applyTo === "ALL"
                                    ? "#eff6ff"
                                    : "#ffffff",
                            color: "#111827",
                            cursor: "pointer",
                            fontWeight: "600",
                            fontSize: "15px"
                        }}
                    >
                        📑 All Pages
                    </button>


                    <button
                        type="button"
                        onClick={() =>
                            setApplyTo("CURRENT")
                        }
                        style={{
                            padding: "15px",
                            borderRadius: "8px",
                            border:
                                applyTo === "CURRENT"
                                    ? "2px solid #2563eb"
                                    : "1px solid #cbd5e1",
                            backgroundColor:
                                applyTo === "CURRENT"
                                    ? "#eff6ff"
                                    : "#ffffff",
                            color: "#111827",
                            cursor: "pointer",
                            fontWeight: "600",
                            fontSize: "15px"
                        }}
                    >
                        📄 Current Page
                    </button>

                </div>
            </div>


            {/* ================================================= */}
            {/* CURRENT PAGE NUMBER */}
            {/* ================================================= */}

            {applyTo === "CURRENT" && (
                <div
                    style={{
                        marginBottom: "28px"
                    }}
                >
                    <label
                        style={{
                            display: "block",
                            marginBottom: "9px",
                            fontWeight: "600",
                            color: "#111827"
                        }}
                    >
                        Page Number
                    </label>

                    <input
                        type="number"
                        min="1"
                        value={pageNumber}
                        onChange={(e) =>
                            setPageNumber(
                                e.target.value
                            )
                        }
                        placeholder="Enter page number"
                        style={{
                            width: "100%",
                            padding: "12px 14px",
                            boxSizing: "border-box",
                            borderRadius: "8px",
                            border: "1px solid #cbd5e1",
                            fontSize: "15px",
                            color: "#111827",
                            backgroundColor: "#ffffff",
                            outline: "none"
                        }}
                    />
                </div>
            )}


            {/* ================================================= */}
            {/* OUTPUT FILE NAME */}
            {/* ================================================= */}

            <div
                style={{
                    marginBottom: "30px"
                }}
            >
                <label
                    style={{
                        display: "block",
                        marginBottom: "9px",
                        fontWeight: "600",
                        color: "#111827"
                    }}
                >
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
                    placeholder="resized.pdf"
                    style={{
                        width: "100%",
                        padding: "12px 14px",
                        boxSizing: "border-box",
                        borderRadius: "8px",
                        border: "1px solid #cbd5e1",
                        fontSize: "15px",
                        color: "#111827",
                        backgroundColor: "#ffffff",
                        outline: "none"
                    }}
                />
            </div>


            {/* ================================================= */}
            {/* PREVIEW */}
            {/* ================================================= */}

            <div
                style={{
                    marginBottom: "30px",
                    textAlign: "center"
                }}
            >
                <h3
                    style={{
                        marginBottom: "18px",
                        color: "#111827"
                    }}
                >
                    Preview
                </h3>

                <div
                    style={{
                        display: "flex",
                        justifyContent: "center"
                    }}
                >
                    <div
                        style={{
                            width: `${preview.width}px`,
                            height: `${preview.height}px`,
                            border:
                                "2px solid #374151",
                            borderRadius: "4px",
                            backgroundColor: "#ffffff",
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            justifyContent: "center",
                            color: "#374151",
                            fontWeight: "600",
                            boxShadow:
                                "0 4px 15px rgba(0,0,0,0.12)",
                            transition:
                                "all 0.2s ease"
                        }}
                    >
                        <div
                            style={{
                                fontSize: "18px",
                                marginBottom: "8px"
                            }}
                        >
                            📄
                        </div>

                        <div>
                            {pageSize}
                        </div>

                        <div
                            style={{
                                fontSize: "13px",
                                marginTop: "5px",
                                color: "#6b7280"
                            }}
                        >
                            {orientation}
                        </div>
                    </div>
                </div>
            </div>


            {/* ================================================= */}
            {/* ERROR */}
            {/* ================================================= */}

            {error && (
                <div
                    style={{
                        padding: "13px 15px",
                        marginBottom: "20px",
                        borderRadius: "8px",
                        backgroundColor: "#fee2e2",
                        color: "#b91c1c",
                        border:
                            "1px solid #fecaca",
                        fontWeight: "500"
                    }}
                >
                    ❌ {error}
                </div>
            )}


            {/* ================================================= */}
            {/* SUCCESS */}
            {/* ================================================= */}

            {message && (
                <div
                    style={{
                        padding: "13px 15px",
                        marginBottom: "20px",
                        borderRadius: "8px",
                        backgroundColor: "#dcfce7",
                        color: "#166534",
                        border:
                            "1px solid #bbf7d0",
                        fontWeight: "500"
                    }}
                >
                    ✅ {message}
                </div>
            )}


            {/* ================================================= */}
            {/* RESIZE BUTTON */}
            {/* ================================================= */}

            <button
                type="button"
                onClick={handleResize}
                disabled={loading}
                style={{
                    width: "100%",
                    padding: "16px",
                    border: "none",
                    borderRadius: "8px",
                    backgroundColor:
                        loading
                            ? "#9ca3af"
                            : "#2563eb",
                    color: "#ffffff",
                    fontSize: "17px",
                    fontWeight: "700",
                    cursor: loading
                        ? "not-allowed"
                        : "pointer",
                    boxShadow:
                        "0 4px 10px rgba(37,99,235,0.25)"
                }}
            >
                {loading
                    ? "⏳ Resizing PDF..."
                    : "📐 Resize PDF"}
            </button>

        </div>
    );
}

export default PdfResize;