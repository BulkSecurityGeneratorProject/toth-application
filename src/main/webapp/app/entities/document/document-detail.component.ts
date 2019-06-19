import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IDocument } from 'app/shared/model/document.model';
import { DocumentService } from 'app/entities/document/document.service';

@Component({
  selector: 'jhi-document-detail',
  templateUrl: './document-detail.component.html'
})
export class DocumentDetailComponent implements OnInit {
  document: IDocument;

  constructor(protected activatedRoute: ActivatedRoute, protected documentService: DocumentService) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ document }) => {
      this.document = document;
    });
  }

  downloadFile() {
    this.documentService.download(this.document.id).subscribe(doc => {
      const fileURL = URL.createObjectURL(doc);
      window.location.href = fileURL;
    });
  }

  previousState() {
    window.history.back();
  }
}