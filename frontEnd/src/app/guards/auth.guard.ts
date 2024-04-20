import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean> {
    const token = this.authService.getToken();

    if (!token) {
      this.router.navigateByUrl('/login');
      return of(false);
    }

    return this.authService.checkToken(token).pipe(
      switchMap((result) => {
        if (result && result.valid) {
          return of(true);
        } else {
          this.authService.logout();
          this.router.navigateByUrl('/login');
          return of(false);
        }
      }),
      catchError(() => {
        this.authService.logout();
        this.router.navigateByUrl('/login');
        return of(false);
      })
    );
  }
}
