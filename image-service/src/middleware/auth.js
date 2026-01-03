export function requireAuth(req, res, next) {
  const auth = req.headers.authorization;
  if (!auth || !auth.startsWith("Bearer ")) {
    return res.status(401).json({ error: "Missing token" });
  }

  const token = auth.substring(7);
  const payload = JSON.parse(
    Buffer.from(token.split(".")[1], "base64").toString()
  );

  req.user = payload;
  next();
}
