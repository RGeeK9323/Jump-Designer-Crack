package jumpdesigner.controller;

import jumpdesigner.document.Document;
import jumpdesigner.sheetmanager.Sheet;

public interface ControllerListener {
  void undoRedoListChanged(boolean paramBoolean1, String paramString1, boolean paramBoolean2, String paramString2);
  
  void newDocumentCreated(Document paramDocument);
  
  void documentOpened(Document paramDocument);
  
  void documentSaved(Document paramDocument);
  
  void documentModified(Document paramDocument);
  
  void documentClosed(Document paramDocument);
  
  void currentEditingSheetChanged(Sheet paramSheet);
  
  void sheetRemoved(Sheet paramSheet);
  
  void sheetAdded(Sheet paramSheet);
  
  void sheetModified(Sheet paramSheet);
  
  void undoProcessed();
  
  void redoProcessed();
}


/* Location:              E:\JumpDesigner\JumpDesigner.jar!\jumpdesigner\controller\ControllerListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */