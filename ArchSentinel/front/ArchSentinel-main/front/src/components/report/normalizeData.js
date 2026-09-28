// report/normalizeData.js

export function normalizeData(data) {
    const result = {
        总体情况: data["总体情况"] || "",
        现存问题: "",
        改进建议: ""
    };

    // 处理现存问题
    result["现存问题"] = Array.isArray(data["现存问题"])
        ? data["现存问题"].join('\n')
        : (data["现存问题"] || "").replace(/(\d+)\.\s*/g, '\n$1.').trim();

    if (result["现存问题"].startsWith('\n')) {
        result["现存问题"] = result["现存问题"].slice(1);
    }

    // 处理改进建议
    result["改进建议"] = Array.isArray(data["改进建议"])
        ? data["改进建议"].join('\n')
        : (data["改进建议"] || "").replace(/(\d+)\.\s*/g, '\n$1.').trim();

    if (result["改进建议"].startsWith('\n')) {
        result["改进建议"] = result["改进建议"].slice(1);
    }

    return result;
}
