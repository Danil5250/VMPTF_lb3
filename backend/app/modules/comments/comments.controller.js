const commentService = require("./comments.service")

module.exports.createComment = async (req, res) => {
    await commentService.createComment(req, res)
}

module.exports.deleteComment = async (req, res) => {
    await commentService.deleteComment(req, res)
}

module.exports.editComment = async (req, res) => {
    await commentService.editComment(req, res)
}