import { Component, inject } from '@angular/core';
import { AumDataService } from '../../services/aum-data';

@Component({
  selector: 'app-upload-modal',
  templateUrl: './upload-modal.html',
  styleUrl: './upload-modal.css',
})
export class UploadModal {
  protected svc = inject(AumDataService);
  selectedFile?: File;

  onBackdropClick(e: MouseEvent): void {
    if ((e.target as HTMLElement).id === 'upload-backdrop') {
      this.svc.closeUpload();
    }
  }

  onFileChange(e: Event): void {
    const input = e.target as HTMLInputElement;
    this.selectedFile = input.files?.[0];
  }

  removeFile(e: MouseEvent): void {
    e.stopPropagation();
    this.selectedFile = undefined;
  }

  onUpload(): void {
    // dummy action for now
    if (!this.selectedFile) {
      alert('Please select a file first.');
      return;
    }
    console.log('uploading', this.selectedFile);
    this.svc.closeUpload();
  }
}
