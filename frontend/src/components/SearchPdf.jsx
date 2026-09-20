
import React, { useState } from "react";

const API_BASE_URL = "http://localhost:8080/api/pdfs";

export default function SearchPdf({ pdfList = [] }) {

    const [selectedPdfId, setSelectedPdfId] =
        useState("");

    const [keyword, setKeyword] =
        useState("");

    const [results, setResults] =
        useState([]);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    const [searched, setSearched] =
        useState(false);

    // =====================================================
    // SELECT PDF
    // =====================================================

    const handlePdfChange = (event) => {

        setSelectedPdfId(
            event.target.value
        );

        setResults([]);
        setError("");
        setSearched(false);
    };

    // =====================================================
    // SEARCH
    // =====================================================

    const handleSearch = async () => {

        if (!selectedPdfId) {

            setError(
                "Please select a PDF file."
            );

            return;
        }

        if (!keyword.trim()) {

            setError(
                "Please enter a word or phrase to search."
            );

            return;
        }

        setLoading(true);
        setError("");
        setResults([]);
        setSearched(false);

        try {

            console.log(
                "Searching PDF ID:",
                selectedPdfId
            );

            console.log(
                "Keyword:",
                keyword
            );

            const response = await fetch(
                `${API_BASE_URL}/search/${selectedPdfId}?keyword=${encodeURIComponent(
        keyword.trim()
)}`
            );

            if (!response.ok) {

                const errorText =
                    await response.text();

                throw new Error(
                    errorText ||
                    "PDF search failed."
                );
            }

            const data =
                await response.json();

            console.log(
                "Search results:",
                data
            );

            setResults(
                Array.isArray(data)
                    ? data
                    : []
            );

            setSearched(true);

        } catch (error) {

            console.error(
                "PDF SEARCH ERROR:",
                error
            );

            setError(
                error.message ||
                "Unable to search the PDF."
            );

        } finally {

            setLoading(false);
        }
    };

    // =====================================================
    // ENTER KEY
    // =====================================================

    const handleKeyDown = (event) => {

        if (event.key === "Enter") {
            handleSearch();
        }
    };

    return (

        <div
            style={{
                width: "100%",
                maxWidth: "700px",
                margin: "0 auto",
                padding: "25px",
                borderRadius: "20px",
                border:
                    "1px solid rgba(255,255,255,0.15)",
                background: "#111a30",
                color: "white",
                boxSizing: "border-box"
            }}
        >

            {/* =================================================
                ICON
            ================================================= */}

            <div
                style={{
                    textAlign: "center",
                    fontSize: "48px",
                    marginBottom: "5px"
                }}
            >
                🔍
            </div>

            <h2
                style={{
                    textAlign: "center",
                    margin: "0 0 8px 0"
                }}
            >
                Search PDF
            </h2>

            <p
                style={{
                    textAlign: "center",
                    color: "#cbd5e1",
                    marginBottom: "28px"
                }}
            >
                Search for words or phrases inside your PDF.
            </p>

            {/* =================================================
                PDF SELECT
            ================================================= */}

            <label
                style={{
                    display: "block",
                    fontWeight: "600",
                    marginBottom: "8px"
                }}
            >
                Select PDF
            </label>

            <select
                value={selectedPdfId}
                onChange={handlePdfChange}
                style={{
                    width: "100%",
                    padding: "13px",
                    borderRadius: "10px",
                    border:
                        "1px solid #52617a",
                    background: "#18243b",
                    color: "white",
                    fontSize: "15px",
                    marginBottom: "20px",
                    boxSizing: "border-box"
                }}
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

            {/* =================================================
                SEARCH INPUT
            ================================================= */}

            <label
                style={{
                    display: "block",
                    fontWeight: "600",
                    marginBottom: "8px"
                }}
            >
                Search keyword
            </label>

            <input
                type="text"
                value={keyword}
                onChange={(event) =>
                    setKeyword(event.target.value)
                }
                onKeyDown={handleKeyDown}
                placeholder="Enter word or phrase..."
                style={{
                    width: "100%",
                    padding: "14px",
                    borderRadius: "10px",
                    border:
                        "1px solid #52617a",
                    background: "#18243b",
                    color: "white",
                    fontSize: "15px",
                    marginBottom: "15px",
                    boxSizing: "border-box",
                    outline: "none"
                }}
            />

            {/* =================================================
                SEARCH BUTTON
            ================================================= */}

            <button
                onClick={handleSearch}
                disabled={loading}
                style={{
                    width: "100%",
                    padding: "14px",
                    border: "none",
                    borderRadius: "10px",
                    background: loading
                        ? "#52617a"
                        : "#2864e8",
                    color: "white",
                    fontSize: "16px",
                    fontWeight: "600",
                    cursor: loading
                        ? "not-allowed"
                        : "pointer"
                }}
            >
                {loading
                    ? "⏳ Searching..."
                    : "🔍 Search PDF"}
            </button>

            {/* =================================================
                ERROR
            ================================================= */}

            {error && (

                <div
                    style={{
                        marginTop: "20px",
                        padding: "14px",
                        borderRadius: "10px",
                        background: "#431c32",
                        color: "#ffffff"
                    }}
                >
                    {error}
                </div>

            )}

            {/* =================================================
                RESULTS
            ================================================= */}

            {searched && !loading && !error && (

                <div
                    style={{
                        marginTop: "25px"
                    }}
                >

                    <div
                        style={{
                            padding: "14px",
                            borderRadius: "10px",
                            background: "#172a4f",
                            marginBottom: "15px",
                            fontWeight: "600"
                        }}
                    >

                        {results.length > 0
                            ? `${results.length} matching result${
    results.length === 1
        ? ""
        : "s"
} found`
                            : `No results found for "${keyword}"`}
                    </div>

                    {results.length > 0 && (

                        <div
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                gap: "12px"
                            }}
                        >

                            {results.map(
                                (result, index) => (

                                    <div
                                        key={index}
                                        style={{
                                            padding: "16px",
                                            borderRadius: "12px",
                                            background: "#18243b",
                                            border:
                                                "1px solid #2d3b55"
                                        }}
                                    >

                                        <div
                                            style={{
                                                fontWeight: "700",
                                                marginBottom: "8px",
                                                color: "#8db7ff"
                                            }}
                                        >
                                            📄 Page {result.page}
                                        </div>

                                        <div
                                            style={{
                                                lineHeight: "1.6",
                                                color: "#e2e8f0"
                                            }}
                                        >
                                            {result.text}
                                        </div>

                                    </div>

                                )
                            )}

                        </div>

                    )}

                </div>

            )}

        </div>
    );
}

