# Peach vs Onion Multi‑Class Image Classifier

## Overview

This project develops a **robust 4‑class computer vision classifier** capable of identifying:

* Peach
* Brown onion
* Purple onion
* Unknown objects

The system demonstrates a **complete deep learning pipeline** from dataset construction and model experimentation to **mobile deployment using TensorFlow Lite**. The goal is to build a classifier that remains accurate under **real‑world lighting, backgrounds, and object orientations**, rather than only controlled lab conditions.

---

## Problem Statement

Distinguishing visually similar produce such as **peaches and onions** becomes difficult under:

* Changing lighting conditions
* Different viewing angles
* Complex or cluttered backgrounds
* Colour ambiguity between brown, red, and purple tones

Traditional shallow CNNs often rely heavily on **colour cues**, leading to incorrect predictions when colour is misleading. This project addresses the challenge by designing architectures that prioritise **shape, texture, and structural features** while maintaining strong generalisation.

---

## Key Features

### 1. Transfer Learning Benchmarking

Comparison of industry‑standard pretrained architectures:

* **InceptionV3**
* **MobileNetV2**
* **ResNet50**

Each model is evaluated under:

* Generic preprocessing (baseline)
* Correct ImageNet preprocessing with stabilised training (improved pipeline)

Metrics compared include **validation accuracy, validation loss, macro F1, and training time**.

---

### 2. Custom CNN Architecture Evolution

Four progressively improved CNN versions were designed:

* **V1 Baseline** – conventional Conv → Flatten → Dense design (overfitting reference)
* **V2 Shape‑First** – VGG‑style double convolutions, BatchNorm, Global Average Pooling
* **V3 Attention‑Enhanced** – Squeeze‑and‑Excitation blocks and Swish activation
* **V4 Hard‑Mining Robustness** – Focal loss, SpatialDropout, deep feature integration

This progression shifts the model from **colour‑matching behaviour** to **structural feature learning**.

---

### 3. Structured Hyperparameter Optimisation

Two complementary tuning strategies were applied:

* **Automated search** (for example Hyperband‑style exploration)
* **Manual macro‑to‑micro refinement** based on validation behaviour

Key optimisations include:

* Learning rate stabilisation for transfer learning
* Balanced augmentation strength for multi‑object safety
* Early stopping and adaptive learning rate scheduling

---

### 4. Dataset Engineering and Generalisation

#### Initial Proxy Dataset

The **TensorFlow Flowers dataset** was used to:

* Validate code correctness
* Establish architecture baselines
* Confirm augmentation and split logic

This ensured later performance changes could be attributed to the **custom dataset**, not implementation errors.

#### Custom Dataset Construction

Final dataset image counts:

* **Peach:** 1201
* **Brown onion:** 1099
* **Purple onion:** 971
* **Unknown:** 1706

**Justification:**

* The **unknown class is intentionally larger** to reflect real‑world deployment where non‑target objects dominate.
* Near‑balanced fruit and onion classes prevent **class bias** while remaining realistic.
* Additional **online images (~50 per class, ~200 unknown)** improve visual diversity and reduce overfitting to camera or background conditions.

#### Data Collection Challenges

Key issues encountered:

* **Burst‑shot images were too similar**, causing severe overfitting
* Limited lighting and backgrounds reduced generalisation

Solutions applied:

* Capture from **multiple angles and distances**
* Use **different lighting environments and surfaces**
* Apply **controlled data augmentation** to simulate real‑world variation

---

### 5. Data Augmentation Strategy

Safe multi‑object augmentation includes:

* Rotation, translation, shear, and zoom (constrained to preserve visibility)
* Horizontal and vertical flips
* Brightness variation without colour corruption

Validation and test pipelines remain **rescale‑only** to ensure unbiased evaluation.

---

### 6. Training Stabilisation Techniques

* Reduced Adam learning rate for transfer learning stability
* EarlyStopping with best‑weight restoration
* ReduceLROnPlateau for adaptive convergence
* ModelCheckpoint for lowest validation loss selection

Validation loss is monitored instead of accuracy to capture **confidence calibration and class separation quality**.

---

### 7. Evaluation Methodology

Comprehensive evaluation includes:

* Test accuracy
* Macro F1 score
* Balanced accuracy
* Confusion matrices
* Classification reports
* Training time comparison

Prediction alignment safeguards ensure **no label mismatch or dropped samples** during testing.

---

### 8. Results Summary

#### Base Pipeline

* MobileNetV2 performed best
* ResNet50 failed due to preprocessing mismatch

#### Improved Pipeline

* **ResNet50 achieved highest accuracy and macro F1**
* **MobileNetV2 delivered best speed‑to‑accuracy trade‑off**
* InceptionV3 incurred highest computational cost

Key insight:

> Correct preprocessing is critical for successful transfer learning.

---

### 9. Mobile Deployment

The final model is converted to **TensorFlow Lite** for:

* Offline inference
* Real‑time mobile detection
* Efficient edge deployment

---

## Repository Structure

```
dataset_stratified_split/
  train/
  val/
  test/

dataset_manual_split/
  train/
  val/
  test/

notebooks/
  peach_onion_training.ipynb

mobile/
  tflite_model/

slides/
  Peach_Onion_Classification_Project.pdf

requirements
  requirements.docx
```

---

## Technologies Used

* Python
* TensorFlow / Keras
* NumPy, Matplotlib, scikit‑learn
* TensorFlow Lite

---

## Future Improvements

* Larger real‑world dataset collection
* On‑device performance benchmarking
* Quantisation for faster mobile inference
* Real‑time camera integration

---

## Author

Developed as an **end‑to‑end deep learning computer vision project** demonstrating:

* Dataset engineering
* Transfer learning research
* CNN architecture design
* Hyperparameter optimisation
* Edge AI deployment

---

## License

This project is released for **educational and research purposes**.
