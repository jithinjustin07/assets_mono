import { Component, inject } from '@angular/core';
import { AumDataService } from '../../services/aum-data';

@Component({
  selector: 'app-navbar',
  imports: [],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  protected svc = inject(AumDataService);

  onSearch(e: Event): void {
    this.svc.setSearch((e.target as HTMLInputElement).value);
  }
}
