const prisma = require("../../config/db")

module.exports.getAllCategories = async (res) => {
    try {
        const result = await prisma.category.findMany()
        res.status(200).json({result})
    }
    catch (error) {
        console.error("Error fetching categories:", error.message);
        res.status(500).json({ error: "Error fetching categories", details: error.message });
    }
}