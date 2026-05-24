const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const prisma = require("../../config/db")
const authService = require("./auth.service")

const JWT_SECRET = process.env.JWT_SECRET

module.exports.register = async (req, res) => {
    const { email, password, name } = req.body;

    const result = authService.checkUserData(email, password, name)
    if (!result.isValid) {
        return res.status(400).json({ error: result.errors.join(", ") });
    }
    const hashedPassword = await bcrypt.hash(password, 10);

    try {
        const user = await prisma.user.create({
        data: { email, name, password: hashedPassword }
        });
        res.json({ message: "User has been created", userId: user.id });
    } catch (e) {
        res.status(400).json({ error: "Email already exists" });
    }
}

module.exports.login = async (req, res) => {
  const { email, password } = req.body;
  
    if(!email || !password) {
        return res.status(400).json({ error: "Email and password are required" });
    }

  const user = await prisma.user.findUnique({ where: { email } });
  if (!user) return res.status(404).json({ error: "User not found" });

  const isMatch = await bcrypt.compare(password, user.password);
  if (!isMatch) return res.status(400).json({ error: "Invalid password" });

  const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '1d' });
  
  res.json({ token, user: { id: user.id, email: user.email, name: user.name, role: user.role } });
};