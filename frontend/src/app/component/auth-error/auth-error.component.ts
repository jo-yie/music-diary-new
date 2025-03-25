import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-auth-error',
  standalone: false,
  templateUrl: './auth-error.component.html',
  styleUrl: './auth-error.component.css'
})
export class AuthErrorComponent implements OnInit {

  errorMessage: string = 'Authentication failed'

  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['error']) {
        this.errorMessage = params['error'];
      }
    });

    // Redirect to dashboard after short delay
    setTimeout(() => {
      this.router.navigate(['/']);
    }, 3000);
  }

}
