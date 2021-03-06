import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Content, IdCid, User } from '../_models';
import { BackendService } from '../_services';
import { NGXLogger } from 'ngx-logger';
import * as $ from 'jquery';

declare var $: any;

@Component({
  selector: 'app-content-viewer',
  templateUrl: './content-viewer.component.html',
  styleUrls: ['./content-viewer.component.scss']
})
export class ContentViewerComponent implements OnInit {

  user: User;

  content: Content;

  constructor(
    private backendService: BackendService,
    private router: Router,
    private route: ActivatedRoute,
    private logger: NGXLogger) { }

  ngOnInit() {
    this.backendService.getUser().subscribe(user => this.user = user);

    let identity = this.route.snapshot.paramMap.get('id');
    let containerId = this.route.snapshot.paramMap.get('cid');

    if (identity && containerId) {
      this.backendService.getModel<Content>(Content, IdCid.of(identity, containerId))
          .toPromise()
          .then(content => {
            this.logger.trace('ContentViewer, content:', content);
            if (Boolean(content) === false) {
              return this.notFound();
            }
            this.content = content;
            $(document).ready(function() {
              $('#sentences').html(content.sentences);
            });
          });
    } else {
      return this.notFound();
    }
  }

  notFound(): Promise<boolean> {
    return this.router.navigate(['/', 'page-not-found']);
  }

  onTag(tag: string): void {
    this.logger.trace("onTag:", tag);
    this.router.navigate(['/', 'search', tag]);
  }

  onEdit(): void {
    this.router.navigate(['/content/edit/', this.content.idCid.identity, this.content.idCid.containerId]);
  }

  onDelete(): void {
    this.logger.trace('onDelete:', this.content);
    this.backendService.deleteModel(Content, this.content.idCid)
      .toPromise()
      .then(() => this.router.navigate(['/user/content/', this.user.identity]));
  }

}
