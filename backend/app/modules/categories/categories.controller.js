const prisma = require("../../config/db")

module.exports.getCategories = async (req, res) => {
    const result = await prisma.category.findMany()
    res.status(200).json({result})
}