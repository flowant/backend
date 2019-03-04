import { Component, OnInit, Input } from '@angular/core';
import { IdCid, Content, Review, Reputation } from '../protocols/model';
import { BackendService } from '../backend.service'
import { Model } from '../config';
import { NGXLogger } from 'ngx-logger';

@Component({
  selector: 'app-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class ReviewComponent implements OnInit {

  @Input() content: Content;

  review: Review;

  reviews: Review[] = new Array<Review>();
  nextInfo: string;

  postedRpt: Reputation;

  constructor(private backendService: BackendService, private logger: NGXLogger) {
  }

  ngOnInit() {
    this.getPopularReviews();
    this.review = new Review();
    this.review.idCid = IdCid.random(this.content.idCid.identity);
  }

  getPopularReviews(): void {
    this.backendService.getPopularItems<Review>(Model.Review, this.content.idCid.identity)
      .subscribe(pupolarReviews => {
        this.reviews = this.reviews.concat(pupolarReviews);
        this.getNextReview();
      });
  }

  getNextReview() {
    this.backendService.getModels<Review>(Model.Review, this.nextInfo, this.content.idCid.identity)
        .subscribe(respWithLink => {
          this.reviews = this.reviews.concat(respWithLink.response);
          this.nextInfo = respWithLink.getNextQueryParams();
          this.logger.trace("nextInfo:", this.nextInfo);
        });
  }

  onSave(): void {
    // check user id and already clicked by user

    this.logger.trace('onSave:', this.review);
    this.backendService.postModel<Review>(Model.Review, this.review)
      .subscribe(() => {});

    //TODO min value of rated is 0 or 1?
    this.review.reputing.reputed = 1;

    let contentRpt = this.postedRpt ?
        this.review.reputing.subtract(this.postedRpt) : this.review.reputing;

    this.logger.trace("onSave, ContentRpt:", contentRpt);
    this.backendService.onRepute(Model.Content, this.content.idCid, undefined, contentRpt)
        .subscribe((rpt) => {
          this.content.reputation = rpt;
          this.postedRpt = Object.assign({}, this.review.reputing);
        });
  }

  onRepute(review: Review, selected: string) {
    this.backendService.onRepute(Model.Review, review.idCid, selected)
        .subscribe((rpt) => {review.reputation = rpt});
  }

}
