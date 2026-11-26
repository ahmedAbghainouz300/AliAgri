import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProviderRowComponent } from './provider-row.component';

describe('ProviderRowComponent', () => {
  let component: ProviderRowComponent;
  let fixture: ComponentFixture<ProviderRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProviderRowComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ProviderRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
