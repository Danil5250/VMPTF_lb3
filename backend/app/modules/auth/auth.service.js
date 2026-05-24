

module.exports.checkUserData = (email, password, name) => {
    const errors = [];
    if (!email) {
        errors.push("Email is required");
    } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            errors.push("Invalid email format");
        }
    }
    if (!password) {
        errors.push("Password is required");
    } else if (password.length < 6) {
        errors.push("Password must be at least 6 characters");
    }
    if (!name) {
        errors.push("Name is required");
    } else if (name.length < 2) {
        errors.push("Name must be at least 2 characters");
    }

    return {
        isValid: errors.length === 0,
        errors
    };
}