export function requireDoctor(req, res, next) {
  const roles = req.user?.realm_access?.roles || [];
  if (!roles.includes("doctor")) {
    return res.status(403).json({ error: "Doctor only" });
  }
  next();
}

export function requirePatientOrDoctor(req, res, next) {
  const roles = req.user?.realm_access?.roles || [];
  if (!roles.includes("doctor") && !roles.includes("patient")) {
    return res.status(403).end();
  }
  next();
}
