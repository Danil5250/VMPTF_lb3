const prisma = require("../../config/db")

module.exports.getCommentsByPost = async (req, res) => {
    try {
        const postId = Number(req.query.postId);
        if (!postId) {
            return res.status(400).json({ error: "postId is required" });
        }
        const comments = await prisma.comment.findMany({
            where: { postId },
            include: { user: { select: { id: true, name: true, email: true } } },
            orderBy: { createdAt: 'asc' }
        });
        res.status(200).json(comments);
    } catch (error) {
        console.error("Error fetching comments:", error.message);
        res.status(500).json({ error: "Error fetching comments", details: error.message });
    }
}

module.exports.createComment = async (req, res) => {
    try {
        const { content, postId, userId } = req.body;
        const newComment = await prisma.comment.create({
            data: {
                content,
                postId: Number(postId),
                userId: userId ? Number(userId) : null
            },
            include: {
                user: true
            }
        });
        res.status(201).json(newComment);
    } catch (error) {
        console.error("Error creating comment:", error.message);
        res.status(500).json({ error: "Error creating comment", details: error.message });
    }
}

module.exports.deleteComment = async (req, res) => {
    try {
        const deletedComment = await prisma.comment.delete({
            where: { id: Number(req.params.id) }
        })
        res.status(200).json({ message: "Comment deleted successfully", deletedComment});
    }
    catch (error) {
        console.error("Error deleting comment:", error.message);
        res.status(500).json({ error: "Error deleting comment", details: error.message });
    }
}

module.exports.editComment = async (req, res) => {
    try {
        await prisma.comment.update({
            where: { id: Number(req.params.id) },
            data: { content: req.body.content }
        });
        res.status(200).json({ content: req.body.content });
    }
    catch (err) {
        console.error("Error editing comment:", err.message);
        res.status(500).json({ error: "Error editing comment", details: err.message });
    }
}