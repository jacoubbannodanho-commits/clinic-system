import express from "express";
import multer from "multer";
import { requireAuth } from "../middleware/auth.js";
import { requireDoctor, requirePatientOrDoctor } from "../middleware/roles.js";
import * as ctrl from "../controllers/images.controller.js";

const router = express.Router();
const upload = multer();

router.post(
  "/patients/:patientId/upload",
  requireAuth,
  requireDoctor,
  upload.single("file"),
  ctrl.uploadForPatient
);

router.post(
  "/:imageId/annotate",
  requireAuth,
  requireDoctor,
  ctrl.annotate
);

router.get(
  "/patients/:patientId/latest",
  requireAuth,
  requirePatientOrDoctor,
  ctrl.getLatest
);

export default router;
