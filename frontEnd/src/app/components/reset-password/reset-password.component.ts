import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CodeInputModule } from 'angular-code-input';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule,CommonModule ,CodeInputModule,RouterModule
  ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent {
  email!: string;
  resetCode!: any;
  newPassword!: string;
  confirmPassword!: string;
  step: number = 1;
  successMessage: string = '';
  errorMessage: string = ''; 

  constructor(private authService: AuthService ,private router:Router , private activatedRoute:ActivatedRoute) {

    this.activatedRoute.data.subscribe((data: any) => {
      const title = data.title || 'Titre par défaut';
      document.title = ` ${title}`;
    });
  }

  requestPasswordReset() {
    if (!this.email || this.email.trim() === '') {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }
   this.errorMessage = '';
    this.authService.requestPasswordReset(this.email).subscribe(
        (response) => {
          this.successMessage = response.message;
          console.log(response)
          setTimeout(() => {
            this.successMessage = ''; 
            this.step = 2;
          }, 2000);
        },
        (error) => {
          console.error('Error:', error);
          this.errorMessage =error.error
          this.successMessage = ''; 
        }
      );
  }
  
  checkResetCode() {
    this.errorMessage = '';
    this.authService.checkResetCode(this.email, this.resetCode).subscribe(
      (response) => {
        console.log(response);
        this.step = 3;
      },
      (error) => {
        console.error('Error:', error);
        this.errorMessage = error.error;
      }
    );
  }
  
resetPassword() {
  this.errorMessage = '';
  this.authService.resetPassword(this.email,this.resetCode,this.newPassword,this.confirmPassword).subscribe(
      (response) => {
        console.log(response.message);
        this.successMessage = response.message;
        setTimeout(() => {
          this.successMessage = '';
      this.router.navigateByUrl("/login")
        }, 2000);
      },
      (error) => {
        console.error('Error:', error);
          this.errorMessage = error.error;
          this.successMessage = '';
      }
    );
}
onCodeChanged(code: string) {
  this.resetCode = code;
}
onCodeCompleted(code: string) {
  this.checkResetCode();
}
}
