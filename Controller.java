/*     */ package jumpdesigner.controller;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Graphics2D;
/*     */ import java.awt.geom.Rectangle2D;
/*     */ import java.awt.image.BufferedImage;
/*     */ import java.awt.print.PageFormat;
/*     */ import java.awt.print.Printable;
/*     */ import java.awt.print.PrinterException;
/*     */ import java.awt.print.PrinterJob;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.event.UndoableEditEvent;
/*     */ import javax.swing.event.UndoableEditListener;
/*     */ import javax.swing.undo.UndoManager;
/*     */ import jumpdesigner.document.Document;
/*     */ import jumpdesigner.document.DocumentListener;
/*     */ import jumpdesigner.gui.MainFrame;
/*     */ import jumpdesigner.gui.actions.NewSheetAction;
/*     */ import jumpdesigner.licencemanager.LicenceManager;
/*     */ import jumpdesigner.licencemanager.LicenceServerCommunicationException;
/*     */ import jumpdesigner.licencemanager.LicenceServerLicenceException;
/*     */ import jumpdesigner.sheetmanager.Layer;
/*     */ import jumpdesigner.sheetmanager.Sheet;
/*     */ import jumpdesigner.sheetmanager.SheetController;
/*     */ import jumpdesigner.sheetmanager.sheetitems.SheetItem;
/*     */ import jumpdesigner.tools.Configuration;
/*     */ import jumpdesigner.tools.GraphicsUtils;
/*     */ import jumpdesigner.tools.Tools;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Controller
/*     */   implements UndoableEditListener, DocumentListener
/*     */ {
/*     */   public static final int LAYER_TAG_HORSE_TRAJECTORY = 1;
/*     */   public static final int LAYER_TAG_FENCES = 2;
/*     */   public static final int LAYER_TAG_FENCE_NUMBERING = 3;
/*     */   public static final int LAYER_TAG_DECOR = 4;
/*     */   public static final int LAYER_TAG_TOOLS = 5;
/*     */   public static final int LAYER_TAG_GROUND = 6;
/*     */   public static final int LAYER_TAG_EXTRA = 7;
/*  50 */   private static Controller controller = new Controller();
/*  51 */   private UndoManager undoManager = new UndoManager();
/*  52 */   private List<ControllerListener> controllerListeners = new LinkedList<ControllerListener>();
/*     */   
/*     */   private Document currentDocument;
/*     */   
/*     */   private Sheet currentEditingSheet;
/*     */   
/*     */   private MainFrame mainFrame;
/*     */   
/*     */   public void addControllerListener(ControllerListener listener) {
/*  61 */     this.controllerListeners.add(listener);
/*     */   }
/*     */   
/*     */   public void removeControllerListener(ControllerListener listener) {
/*  65 */     this.controllerListeners.remove(listener);
/*     */   }
/*     */   
/*     */   public void fireNewDocumentCreated() {
/*  69 */     for (ControllerListener listener : this.controllerListeners) {
/*  70 */       listener.newDocumentCreated(this.currentDocument);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireDocumentOpened() {
/*  75 */     for (ControllerListener listener : this.controllerListeners) {
/*  76 */       listener.documentOpened(this.currentDocument);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireDocumentSaved() {
/*  81 */     for (ControllerListener listener : this.controllerListeners) {
/*  82 */       listener.documentSaved(this.currentDocument);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireDocumentModified() {
/*  87 */     for (ControllerListener listener : this.controllerListeners) {
/*  88 */       listener.documentModified(this.currentDocument);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireDocumentClosed() {
/*  93 */     for (ControllerListener listener : this.controllerListeners) {
/*  94 */       listener.documentClosed(this.currentDocument);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireUndoRedoListChanged() {
/*  99 */     for (ControllerListener listener : this.controllerListeners) {
/* 100 */       listener.undoRedoListChanged(this.undoManager.canUndo(), this.undoManager.getUndoPresentationName(), this.undoManager.canRedo(), this.undoManager.getRedoPresentationName());
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireCurrentEditingSheetChanged() {
/* 105 */     for (ControllerListener listener : this.controllerListeners) {
/* 106 */       listener.currentEditingSheetChanged(this.currentEditingSheet);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireSheetAdded(Sheet sheet) {
/* 111 */     for (ControllerListener listener : this.controllerListeners) {
/* 112 */       listener.sheetAdded(sheet);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireSheetRemoved(Sheet sheet) {
/* 117 */     for (ControllerListener listener : this.controllerListeners) {
/* 118 */       listener.sheetRemoved(sheet);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireSheetModified(Sheet sheet) {
/* 123 */     for (ControllerListener listener : this.controllerListeners) {
/* 124 */       listener.sheetModified(sheet);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireUndoProcessed() {
/* 129 */     for (ControllerListener listener : this.controllerListeners) {
/* 130 */       listener.undoProcessed();
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireRedoProcessed() {
/* 135 */     for (ControllerListener listener : this.controllerListeners) {
/* 136 */       listener.redoProcessed();
/*     */     }
/*     */   }
/*     */   
/*     */   public void setCurrentEditingSheet(Sheet currentEditingSheet) {
/* 141 */     if (this.currentEditingSheet != currentEditingSheet) {
/* 142 */       this.currentEditingSheet = currentEditingSheet;
/* 143 */       fireCurrentEditingSheetChanged();
/*     */     } 
/*     */   }
/*     */   
/*     */   public Sheet getCurrentEditingSheet() {
/* 148 */     return this.currentEditingSheet;
/*     */   }
/*     */   
/*     */   public UndoManager getUndoManager() {
/* 152 */     return this.undoManager;
/*     */   }
/*     */ 
/*     */   
/*     */   public void undoableEditHappened(UndoableEditEvent evt) {
/* 157 */     fireUndoRedoListChanged();
/*     */   }
/*     */   
/*     */   public static Controller getInstance() {
/* 161 */     return controller;
/*     */   }
/*     */   
/*     */   public Document getCurrentDocument() {
/* 165 */     return this.currentDocument;
/*     */   }
/*     */   
/*     */   public void setMainFrame(MainFrame mainFrame) {
/* 169 */     this.mainFrame = mainFrame;
/*     */   }
/*     */   
/*     */   public void saveConfiguration() {
/*     */     try {
/* 174 */       Configuration.save();
/* 175 */     } catch (Exception ex) {
/* 176 */       ex.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void newFrame() {
/* 181 */     if (null != this.mainFrame) {
/*     */       
/* 183 */       saveConfiguration();
/* 184 */       this.mainFrame.dispose();
/*     */     } 
/* 186 */     setMainFrame(new MainFrame());
/* 187 */     this.mainFrame.setVisible(true);
/*     */   }
/*     */   
/*     */   public void exitApplication() {
/* 191 */     saveConfiguration();
/* 192 */     System.exit(0);
/*     */   }
/*     */   
/*     */   public void undo() {
/* 196 */     if (canUndo()) {
/* 197 */       this.undoManager.undo();
/* 198 */       fireUndoRedoListChanged();
/* 199 */       fireUndoProcessed();
/* 200 */       this.currentDocument.setModified(true);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void redo() {
/* 205 */     if (canRedo()) {
/* 206 */       this.undoManager.redo();
/* 207 */       fireUndoRedoListChanged();
/* 208 */       fireRedoProcessed();
/* 209 */       this.currentDocument.setModified(true);
/*     */     } 
/*     */   }
/*     */   
/*     */   public boolean canUndo() {
/* 214 */     return this.undoManager.canUndo();
/*     */   }
/*     */   
/*     */   public boolean canRedo() {
/* 218 */     return this.undoManager.canRedo();
/*     */   }
/*     */   
/*     */   public void clearUndoRedoList() {
/* 222 */     this.undoManager.discardAllEdits();
/* 223 */     fireUndoRedoListChanged();
/*     */   }
/*     */   
/*     */   public void createNewDocument() {
/*     */     try {
/* 228 */       documentSavedCheck();
/* 229 */       closeDocument();
/* 230 */       this.currentDocument = Document.create();
/* 231 */       this.currentDocument.addDocumentListener(this);
/*     */       
/* 233 */       (new NewSheetAction()).actionPerformed(null);
/*     */       
/* 235 */       clearUndoRedoList();
/*     */       
/* 237 */       this.currentDocument.setModified(false);
/* 238 */       fireNewDocumentCreated();
/* 239 */     } catch (Exception ex) {
/* 240 */       ex.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void openDocument() {
/* 245 */     openDocument(null);
/*     */   }
/*     */   
/*     */   public void openDocument(File file) {
/*     */     try {
/* 250 */       documentSavedCheck();
/* 251 */       if (file == null) {
/* 252 */         JFileChooser fileChooser = new JFileChooser(".");
/* 253 */         fileChooser.setDialogTitle(Tools.getString("JAAJ..."));
/* 254 */         fileChooser.setFileFilter(Document.getFileFilter());
/* 255 */         fileChooser.setSelectedFile((this.currentDocument != null) ? this.currentDocument.getFile() : Configuration.getDefaultDirectory());
/* 256 */         if (fileChooser.showOpenDialog(null) == 0) {
/* 257 */           file = fileChooser.getSelectedFile();
/*     */         }
/*     */       } 
/* 260 */       if (file != null) {
/*     */         try {
/* 262 */           Document newDocument = Document.open(file);
/* 263 */           closeDocument();
/* 264 */           this.currentDocument = newDocument;
/* 265 */           this.currentDocument.addDocumentListener(this);
/*     */           
/* 267 */           clearUndoRedoList();
/*     */           
/* 269 */           this.currentDocument.setModified(false);
/* 270 */           fireDocumentOpened();
/* 271 */         } catch (Exception ex) {
/* 272 */           ex.printStackTrace();
/* 273 */           JOptionPane.showMessageDialog(null, Tools.getString("IMPOSSIBLE_D'OUVRIR_LE_DOCUMENT_:") + "\n" + ex, Tools.getString("ERREUR..."), 0);
/*     */         } 
/*     */       }
/* 276 */     } catch (Exception ex) {
/* 277 */       ex.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void saveDocument() throws Exception {
/* 282 */     if (this.currentDocument != null && LicenceManager.checkRegistration()) {
/*     */       try {
/* 284 */         this.currentDocument.save();
/* 285 */         fireDocumentSaved();
/* 286 */       } catch (Exception ex) {
/* 287 */         ex.printStackTrace();
/* 288 */         JOptionPane.showMessageDialog(null, Tools.getString("IMPOSSIBLE_DE_SAUVEGARDER_LE_DOCUMENT_:") + "\n" + ex, Tools.getString("ERREUR..."), 0);
/* 289 */         throw ex;
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   public void saveDocumentAs() throws Exception {
/* 295 */     if (this.currentDocument != null && LicenceManager.checkRegistration()) {
/* 296 */       JFileChooser fileChooser = new JFileChooser(Configuration.defaultDirectory);
/* 297 */       fileChooser.setDialogTitle(Tools.getString("ENREGISTRER..."));
/* 298 */       fileChooser.setFileFilter(Document.getFileFilter());
/* 299 */       File oldFile = this.currentDocument.getFile();
/* 300 */       fileChooser.setSelectedFile((this.currentDocument != null) ? oldFile : Configuration.getDefaultDirectory());
/* 301 */       if (fileChooser.showSaveDialog(null) == 0) {
/* 302 */         File file = fileChooser.getSelectedFile();
/* 303 */         if (file.getName().indexOf('.') == -1) {
/* 304 */           file = new File(file.getAbsolutePath() + "." + Document.getFileExtension());
/*     */         }
/* 306 */         if (file.exists()) {
/* 307 */           switch (JOptionPane.showConfirmDialog(null, Tools.getString("LE_FICHIER_EXISTE_DÉJÀ._VOULEZ-VOUS_LE_REMPLACER_?"), Tools.getString("CONFIRMATION..."), 1)) {
/*     */             case 1:
/* 309 */               saveDocumentAs();
/*     */               return;
/*     */             case 2:
/* 312 */               throw new Exception();
/*     */           } 
/*     */         }
/* 315 */         this.currentDocument.setFile(file);
/*     */         try {
/* 317 */           saveDocument();
/* 318 */         } catch (Exception ex) {
/* 319 */           this.currentDocument.setFile(oldFile);
/* 320 */           throw ex;
/*     */         } 
/*     */       } else {
/* 323 */         throw new Exception();
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public void closeDocument() {
/* 329 */     if (this.currentDocument != null) {
/* 330 */       this.currentDocument.close();
/* 331 */       fireDocumentClosed();
/* 332 */       this.currentDocument = null;
/*     */     } 
/* 334 */     setCurrentEditingSheet(null);
/* 335 */     clearUndoRedoList();
/*     */   }
/*     */   
/*     */   public void documentSavedCheck() throws Exception {
/* 339 */     if (this.currentDocument != null && this.currentDocument.isModified()) {
/* 340 */       switch (JOptionPane.showConfirmDialog(null, Tools.getString("LE_DOCUMENT_A_ÉTÉ_MODIFIÉ._VOULEZ-VOUS_SAUVEGARDER_LES_MODIFICATION_?"), Tools.getString("CONFIRMATION..."), 1)) {
/*     */         case 0:
/* 342 */           if (this.currentDocument.isSavedAtLeastOnce()) {
/* 343 */             saveDocument(); break;
/*     */           } 
/* 345 */           saveDocumentAs();
/*     */           break;
/*     */         
/*     */         case 2:
/* 349 */           throw new Exception();
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   public void removeCurrentEditingSheet() {
/* 355 */     Sheet sheet = this.currentEditingSheet;
/* 356 */     this.currentDocument.beginEdit();
/* 357 */     this.currentDocument.removeSheet(sheet);
/* 358 */     this.currentDocument.endEdit();
/* 359 */     setCurrentEditingSheet(null);
/* 360 */     fireSheetRemoved(sheet);
/* 361 */     this.currentDocument.setModified(true);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void createNewSheet(double width, double height, Color iconColor, double defaultFenceBarHeight, double defaultFenceCandelabraSpacing, String testLocation, Date testDateTime, String testNumber, String testType, String testCategory, String testIndice, String testScalePart1, String testScalePart2, double testSpeed, String testPrice, String testDamFenceList, double testDistancePart1, double testDistancePart2, String testArticle, int gridContrast, String comment, String fontsize, String fontsize1, boolean isinternational, int Pointcontrast, BufferedImage logoPartenaire) {
/* 392 */     this.currentDocument.beginEdit();
/*     */     
/* 394 */     Sheet sheet = new Sheet(new SheetController(this.currentDocument));
/* 395 */     sheet.setWidth(width);
/* 396 */     sheet.setHeight(height);
/* 397 */     sheet.setIconColor(iconColor);
/* 398 */     sheet.setRenderingQuality(Configuration.getRenderingQuality());
/* 399 */     sheet.setXGridOffset(width / 2.0D);
/* 400 */     sheet.setYGridOffset(height / 2.0D);
/* 401 */     sheet.setDefaultFenceBarHeight(defaultFenceBarHeight);
/* 402 */     sheet.setDefaultFenceCandelabraSpacing(defaultFenceCandelabraSpacing);
/* 403 */     sheet.setTestLocation(testLocation);
/* 404 */     sheet.setTestDateTime(testDateTime);
/* 405 */     sheet.setTestNumber(testNumber);
/* 406 */     sheet.setTestType(testType);
/* 407 */     sheet.setTestCategory(testCategory);
/* 408 */     sheet.setTestIndice(testIndice);
/* 409 */     sheet.setTestScalePart1(testScalePart1);
/* 410 */     sheet.setTestScalePart2(testScalePart2);
/* 411 */     sheet.setTestSpeed(testSpeed);
/* 412 */     sheet.setTestPrice(testPrice);
/* 413 */     sheet.setTestDamFenceList(testDamFenceList);
/* 414 */     sheet.setTestDistancePart1(testDistancePart1);
/* 415 */     sheet.setTestDistancePart2(testDistancePart2);
/* 416 */     sheet.setTestArticle(testArticle);
/* 417 */     sheet.setGridContrast(gridContrast);
/* 418 */     sheet.setComment(comment);
/* 419 */     sheet.setLogoPartenaire(logoPartenaire);
/* 420 */     this.currentDocument.setfontsize(fontsize);
/* 421 */     this.currentDocument.setfontsize1(fontsize1);
/* 422 */     this.currentDocument.setisinternational(isinternational);
/* 423 */     this.currentDocument.setPointcontrast(Pointcontrast);
/* 424 */     this.currentDocument.addSheet(sheet);
/* 425 */     this.currentDocument.endEdit();
/* 426 */     fireSheetAdded(sheet);
/* 427 */     this.currentDocument.setModified(true);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void modifyCurrentEditingSheet(double width, double height, Color iconColor, double defaultFenceBarHeight, double defaultFenceCandelabraSpacing, String testLocation, Date testDateTime, String testNumber, String testType, String testCategory, String testIndice, String testScalePart1, String testScalePart2, double testSpeed, String testPrice, String testDamFenceList, double testDistancePart1, double testDistancePart2, String testArticle, int gridContrast, String comment, String fontsize, String fontsize1, boolean isinternational, int Pointcontrast, BufferedImage logoPartenaire) {
/* 458 */     this.currentEditingSheet.beginEdit();
/* 459 */     this.currentEditingSheet.setWidth(width);
/* 460 */     this.currentEditingSheet.setHeight(height);
/* 461 */     this.currentEditingSheet.setIconColor(iconColor);
/* 462 */     this.currentEditingSheet.setDefaultFenceBarHeight(defaultFenceBarHeight);
/* 463 */     this.currentEditingSheet.setDefaultFenceCandelabraSpacing(defaultFenceCandelabraSpacing);
/* 464 */     this.currentEditingSheet.setTestLocation(testLocation);
/* 465 */     this.currentEditingSheet.setTestDateTime(testDateTime);
/* 466 */     this.currentEditingSheet.setTestNumber(testNumber);
/* 467 */     this.currentEditingSheet.setTestType(testType);
/* 468 */     this.currentEditingSheet.setTestCategory(testCategory);
/* 469 */     this.currentEditingSheet.setTestIndice(testIndice);
/* 470 */     this.currentEditingSheet.setTestScalePart1(testScalePart1);
/* 471 */     this.currentEditingSheet.setTestScalePart2(testScalePart2);
/* 472 */     this.currentEditingSheet.setTestSpeed(testSpeed);
/* 473 */     this.currentEditingSheet.setTestPrice(testPrice);
/* 474 */     this.currentEditingSheet.setTestDamFenceList(testDamFenceList);
/* 475 */     this.currentEditingSheet.setTestDistancePart1(testDistancePart1);
/* 476 */     this.currentEditingSheet.setTestDistancePart2(testDistancePart2);
/* 477 */     this.currentEditingSheet.setTestArticle(testArticle);
/* 478 */     this.currentEditingSheet.setGridContrast(gridContrast);
/* 479 */     this.currentEditingSheet.setComment(comment);
/* 480 */     this.currentDocument.setfontsize(fontsize);
/* 481 */     this.currentDocument.setfontsize1(fontsize1);
/* 482 */     this.currentDocument.setisinternational(isinternational);
/* 483 */     this.currentDocument.setPointcontrast(Pointcontrast);
/* 484 */     this.currentEditingSheet.setLogoPartenaire(logoPartenaire);
/* 485 */     this.currentEditingSheet.endEdit();
/* 486 */     fireSheetModified(this.currentEditingSheet);
/* 487 */     this.currentDocument.setModified(true);
/*     */   }
/*     */   
/*     */   public void addSheetItemToCurrentEditingSheet(SheetItem sheetItem) {
/* 491 */     Layer layer = this.currentEditingSheet.findLayerByTag(sheetItem.getTag());
/* 492 */     if (layer != null) {
/*     */       
/* 494 */       this.currentEditingSheet.exitExclusiveMode();
/* 495 */       SheetController sheetController = this.currentEditingSheet.getSheetController();
/* 496 */       layer.beginEdit();
/* 497 */       sheetItem.setLayer(layer);
/* 498 */       layer.addSheetItem(sheetItem);
/* 499 */       layer.endEdit();
/*     */       
/* 501 */       sheetController.unselectAllSheetItems();
/* 502 */       sheetController.selectSheetItem(sheetItem);
/* 503 */       this.currentEditingSheet.repaint(sheetItem.getBouncingBox());
/* 504 */       this.currentDocument.setModified(true);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void duplicateCurrentEditingSheet(Set<Integer> tagsToCopy) {
/* 515 */     this.currentDocument.beginEdit();
/* 516 */     Sheet sheet = this.currentEditingSheet.createCopy(tagsToCopy, this.currentDocument);
/* 517 */     this.currentDocument.addSheet(sheet);
/* 518 */     this.currentDocument.endEdit();
/* 519 */     fireSheetAdded(sheet);
/* 520 */     this.currentDocument.setModified(true);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void printSheet(final List<Sheet> sheets) {
/* 529 */     PrinterJob printerJob = PrinterJob.getPrinterJob();
/* 530 */     printerJob.setJobName(Configuration.getApplicationName());
/* 531 */     printerJob.setPrintable(new Printable()
/*     */         {
/*     */           public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
/* 534 */             if (pageIndex < sheets.size()) {
/* 535 */               Sheet sheet = sheets.get(pageIndex);
/*     */               
/* 537 */               double dpi = 150.0D;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 543 */               Rectangle2D pageBounds = new Rectangle2D.Double(0.0D, 0.0D, pageFormat.getWidth(), pageFormat.getHeight());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */               
/* 549 */               Rectangle2D imageableBounds = new Rectangle2D.Double(pageFormat.getImageableX(), pageFormat.getImageableY(), pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
/* 550 */               Graphics2D g = (Graphics2D)graphics;
/* 551 */               GraphicsUtils.applyRenderingQuality(g, 3);
/*     */ 
/*     */               
/* 554 */               if (pageFormat.getOrientation() == 1) {
/*     */                 
/* 556 */                 g.translate(pageBounds.getWidth(), 0.0D);
/* 557 */                 g.rotate(1.5707963267948966D);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */                 
/* 563 */                 imageableBounds = new Rectangle2D.Double(imageableBounds.getY(), pageBounds.getWidth() - imageableBounds.getX() - imageableBounds.getWidth(), imageableBounds.getHeight(), imageableBounds.getWidth());
/*     */ 
/*     */ 
/*     */ 
/*     */                 
/* 568 */                 pageBounds = new Rectangle2D.Double(0.0D, 0.0D, pageBounds.getHeight(), pageBounds.getWidth());
/*     */               } 
/* 570 */               sheet.print(g, pageBounds, imageableBounds, dpi);
/* 571 */               return 0;
/*     */             } 
/* 573 */             return 1;
/*     */           }
/*     */         });
/* 576 */     if (printerJob.printDialog()) {
/*     */       try {
/* 578 */         printerJob.print();
/* 579 */       } catch (Exception ex) {
/* 580 */         JOptionPane.showMessageDialog(null, ex, Tools.getString("ERREUR..."), 0);
/* 581 */         ex.printStackTrace();
/*     */       } 
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public void documentModifiedStatusChanged(Document document, boolean modified) {
/* 588 */     if (modified) {
/* 589 */       fireDocumentModified();
/*     */     }
/*     */   }
/*     */   
/*     */   public void removeAllLayerSheetItems(Layer layer) {
/* 594 */     List<SheetItem> sheetItems = new LinkedList<SheetItem>(layer.getSheetItems());
/* 595 */     if (sheetItems.size() > 0) {
/* 596 */       layer.beginEdit();
/* 597 */       for (SheetItem sheetItem : sheetItems) {
/* 598 */         layer.removeSheetItem(sheetItem);
/*     */       }
/* 600 */       layer.endEdit();
/* 601 */       fireDocumentModified();
/* 602 */       layer.getSheet().repaint();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void tidyLayerSheetItems(Layer layer) {
/* 607 */     double x = layer.getSheet().getXGridOffset();
/* 608 */     double y = layer.getSheet().getYGridOffset();
/* 609 */     int i = 0;
/* 610 */     for (SheetItem sheetItem : layer.getSheetItems()) {
/* 611 */       sheetItem.beginEdit();
/* 612 */       sheetItem.setX(x);
/* 613 */       sheetItem.setY(y);
/* 614 */       sheetItem.endEdit();
/* 615 */       x += 2000.0D;
/* 616 */       if (i > 0 && (i + 1) % 5 == 0) {
/* 617 */         x = layer.getSheet().getXGridOffset();
/* 618 */         y += 2000.0D;
/*     */       } 
/* 620 */       i++;
/*     */     } 
/* 622 */     layer.getSheet().repaint();
/*     */   }
/*     */   
/*     */   public void registerApplication(String licenceKey) {
/*     */     try {
/* 627 */       if (!LicenceManager.licenceKeyIsValid(licenceKey)) {
/* 628 */         throw new LicenceServerLicenceException();
/*     */       }
/* 630 */       String newValidationKey = LicenceManager.validateInstallation(licenceKey, LicenceManager.getInstallationKey(), LicenceManager.getProductKey());
/* 631 */       Configuration.setLicenceKey(licenceKey);
/* 632 */       Configuration.setValidationKey(newValidationKey);
/* 633 */       Configuration.save();
/* 634 */       LicenceManager.setRegistered(true);
/* 635 */       JOptionPane.showMessageDialog(null, Tools.getString("VOTRE_COPIE_DU_LOGICIEL_A_BIEN_ÉTÉ_ENREGISTRÉE._MERCI."), Tools.getString("INFORMATION..."), 1);
/* 636 */     } catch (LicenceServerCommunicationException ex) {
/* 637 */       ex.printStackTrace();
/* 638 */       JOptionPane.showMessageDialog(null, Tools.getString("IMPOSSIBLE_DE_SE_CONNECTER_AU_SERVEUR_DE_LICENCE"), Tools.getString("ERREUR..."), 0);
/* 639 */     } catch (LicenceServerLicenceException ex) {
/* 640 */       ex.printStackTrace();
/* 641 */       JOptionPane.showMessageDialog(null, Tools.getString("NUMÉRO_DE_LICENCE_NON_VALIDE"), Tools.getString("ERREUR..."), 0);
/* 642 */     } catch (IOException ex) {
/* 643 */       ex.printStackTrace();
/* 644 */       JOptionPane.showMessageDialog(null, Tools.getString("IMPOSSIBLE_DE_SAUVEGARDER_LA_LICENCE"), Tools.getString("ERREUR..."), 0);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              E:\JumpDesigner\JumpDesigner.jar!\jumpdesigner\controller\Controller.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */