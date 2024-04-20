import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { User } from '../../models/user';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [FormsModule , CommonModule , ReactiveFormsModule,RouterModule ],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  registrationDto: User = {
    firstname: '',
    lastname: '',
    email: '',
    password: '',
    token: null
  };
    successMessage: string = '';
  errorMessage: string = ''; 

  constructor( private activatedRoute:ActivatedRoute , private authService:AuthService){

  }
  ngOnInit(): void {

    this.activatedRoute.data.subscribe((data: any) => {
      const title = data.title || 'Titre par défaut';
      document.title = ` ${title}`;
    });

 
  }

  Signup() {
    this.authService.register(this.registrationDto).subscribe(
      response => {
        this.successMessage = response.message;
        this.errorMessage =""
        this.registrationDto = { firstname: '', lastname: '', email: '', password: '', token: null };
        console.log('Registration successful:', response);
      },
      error => {
        this.errorMessage = error.error.error;
        this.successMessage ="";
        console.error('Registration error:', error);
      }
    );
  }
  

}
