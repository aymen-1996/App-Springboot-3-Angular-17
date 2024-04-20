import { Component } from '@angular/core';
import { User } from '../../models/user';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-project',
  standalone: true,
  imports: [CommonModule  , DragDropModule , RouterModule
  ],
  templateUrl: './project.component.html',
  styleUrl: './project.component.css'
})
export class ProjectComponent {
  users: User[] = [];

  constructor(private userService: AuthService , private activatedRoute:ActivatedRoute ,private router:Router ) { }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe((data: any) => {
      const title = data.title || 'Titre par défaut';
      document.title = ` ${title}`;
    });
  
    this.getAllUser()
  }


  getAllUser(){
      
    this.userService.getUsers().subscribe(users => {
      this.users = users;
      console.log("userrrr", users)
    });
  }

  drop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.users, event.previousIndex, event.currentIndex);

    this.users.forEach((user, index) => user.orderNumber = index + 1);

    this.userService.reorderUsers(this.users).subscribe(() => {
        console.log('Ordre des utilisateurs mis à jour avec succès');
    }, error => {
        console.error('Erreur lors de la mise à jour de l\'ordre des utilisateurs :', error);
    });
}

deleteUser(userId: any): void {
  const confirmDelete = confirm('Are you sure you want to delete this user?');
  if (confirmDelete) {
    this.userService.deleteUser(userId)
      .subscribe(
        () => {
          this.getAllUser()
          console.log('User deleted successfully');
        },
        error => {
          console.error('Error deleting user:', error);
        }
      );
  }
}
 logout() {
    localStorage.removeItem('currentUser');

    this.router.navigateByUrl("login")
  }
}
