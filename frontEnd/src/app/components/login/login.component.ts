import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule , CommonModule , ReactiveFormsModule,RouterModule ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  showFirst: boolean = false;
  email: string = '';
  password: string = '';
  errorMessage: string = ''
  formUser!: FormGroup
  constructor(private router:Router , private authService: AuthService , private formBuilder: FormBuilder , private activatedRoute:ActivatedRoute){
    if (this.authService.currentUserValue ) {
      this.router.navigate(['/projects']);
    }
  }
  ngOnInit(): void {

    this.activatedRoute.data.subscribe((data: any) => {
      const title = data.title || 'Titre par défaut';
      document.title = ` ${title}`;
    });

    this.formUser = this.formBuilder.group({

      email:'',

      password: '',
    });
  }


  Login(): void {
    this.authService.login(this.formUser.value.email, this.formUser.value.password).subscribe(
      (response) => {
        if (response && response.token) {
          this.router.navigate(['/projects']);
          console.log(response);
        } else {
          this.errorMessage = response.error;
          console.error('Unexpected response format:', response);
        }
      },
      (error) => {
        console.error('Login error:', error);
        this.errorMessage = 'An unexpected error occurred.';
      }
    );
  }
  
  
  

   onShowFirstChange(value: boolean) {
    this.showFirst = value;
  }
}

