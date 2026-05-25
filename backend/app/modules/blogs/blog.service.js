const prisma = require("../../config/db")
const { clearCache } = require("../middleware/cashing")

module.exports.createPost = async (req, res) => {
    const { title, content, author, categoryIds } = req.body;

    try {
    const newPost = await prisma.post.create({
      data: {
        title,
        content,
        author,
        categories: {
          connect: categoryIds?.map(id => ({ id })) || []
        }
      },
      include: {
        categories: true //return post with its categories
      }
    });
    res.status(201).json(newPost);
    clearCache();
  } catch (error) {
    res.status(500).json({ error: "Error creating post", details: error.message });
  }
}

module.exports.getAllPosts = async (res) => {
    try {
        const result = await prisma.post.findMany({
          include: {
            categories: true,
          },
          orderBy: {
            createdAt: 'desc' 
          }
        })
        res.status(200).json({result})
    }
    catch (error) {
        console.error("Error fetching posts:", error.message);
        res.status(500).json({ error: "Error fetching posts", details: error.message });
    }
}

module.exports.getPostById = async (req, res) => {
  try {
    const { id } = req.params;
    const post = await prisma.post.findUnique({
      where: { id: Number(id) },
      include: { 
        categories: true, //include categories
        comments: {
          include: {
            user: true
          },
          orderBy: {
            createdAt: 'desc'
          }
        }
      }
    });
    if (!post) {
      return res.status(404).json({ error: "Post not found" });
    }
    res.status(200).json(post);
  }
  catch (error) {
    console.error("Error fetching post by id:", error.message);
    res.status(500).json({ error: "Error fetching posts", details: error.message });
  }
}

module.exports.getPostFilteredSearched = async (req, res) => {
  try {
    const { search, categoryIds } = req.query;
    const where = {};
    if(search) {
      where.OR = [
        { title: { contains: search, mode: 'insensitive' } },
        { content: { contains: search, mode: 'insensitive' } }
      ];
    }
    if(categoryIds) {
      console.log("Received categoryIds:", categoryIds);
      const ids = Array.isArray(categoryIds) 
      ? categoryIds.map(id => parseInt(id)) 
      : [parseInt(categoryIds)];
      
      where.categories = {
        some: {
          id: { in: ids }
        }
      };
    }
    const posts = await prisma.post.findMany({ 
      where,
      include: {
        categories: true,
      },
      orderBy: {
        createdAt: 'desc' 
      }
    });
    res.status(200).json(posts);
    } catch (error) {
      console.error("Error fetching filtered and searched posts:", error.message);
      res.status(500).json({ error: "Error fetching posts", details: error.message });
    }
}

module.exports.updatePost = async (req, res) => {
  try {
    const { id } = req.params;
    const { title, content, author, categoryIds } = req.body;

    const existing = await prisma.post.findUnique({ where: { id: Number(id) } });
    if (!existing) {
      return res.status(404).json({ error: "Post not found" });
    }

    const updatedPost = await prisma.post.update({
      where: { id: Number(id) },
      data: {
        ...(title !== undefined && { title }),
        ...(content !== undefined && { content }),
        ...(author !== undefined && { author }),
        ...(categoryIds !== undefined && {
          categories: {
            set: categoryIds.map(catId => ({ id: catId }))
          }
        })
      },
      include: {
        categories: true,
        comments: true
      }
    });

    res.status(200).json(updatedPost);
    clearCache();
  } catch (error) {
    console.error("Error updating post:", error.message);
    res.status(500).json({ error: "Error updating post", details: error.message });
  }
}

module.exports.deletePost = async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await prisma.post.findUnique({ where: { id: Number(id) } });
    if (!existing) {
      return res.status(404).json({ error: "Post not found" });
    }

    await prisma.post.delete({
      where: { id: Number(id) }
    });

    res.status(200).json({ message: "Post deleted successfully" });
    clearCache();
  } catch (error) {
    console.error("Error deleting post:", error.message);
    res.status(500).json({ error: "Error deleting post", details: error.message });
  }
}