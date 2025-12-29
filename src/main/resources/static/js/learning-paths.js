// Learning Paths - Enhanced JavaScript with Animations

// ========== Toast Notification System ==========
function showToast(message, type = 'info', duration = 3000) {
    const toast = document.createElement('div');
    toast.className = `toast-notification ${type}`;
    toast.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'} me-2"></i>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(toast);
    
    // Animate in
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    }, 10);
    
    // Remove after duration
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100px)';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// ========== Smooth Scroll to Element ==========
function smoothScrollTo(element, offset = 0) {
    const elementPosition = element.getBoundingClientRect().top + window.pageYOffset;
    const offsetPosition = elementPosition - offset;
    
    window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
    });
}

// ========== Progress Bar Animation ==========
function animateProgressBar(progressBar, targetPercent) {
    const currentPercent = parseInt(progressBar.style.width) || 0;
    const increment = targetPercent > currentPercent ? 1 : -1;
    
    const animate = () => {
        if ((increment > 0 && currentPercent < targetPercent) || 
            (increment < 0 && currentPercent > targetPercent)) {
            progressBar.style.width = currentPercent + '%';
            requestAnimationFrame(animate);
        } else {
            progressBar.style.width = targetPercent + '%';
        }
    };
    
    animate();
}

// ========== Card Intersection Observer ==========
function observeCards() {
    const cards = document.querySelectorAll('.path-card, .step-card');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });
    
    cards.forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(card);
    });
}

// ========== Number Counter Animation ==========
function animateCounter(element, target, duration = 2000) {
    const start = 0;
    const increment = target / (duration / 16);
    let current = start;
    
    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            element.textContent = Math.round(target);
            clearInterval(timer);
        } else {
            element.textContent = Math.round(current);
        }
    }, 16);
}

// ========== Loading Skeleton ==========
function showSkeleton(container, count = 3) {
    const skeletonHTML = `
        <div class="skeleton-card premium-card p-4 mb-3">
            <div class="skeleton skeleton-title mb-3"></div>
            <div class="skeleton skeleton-text mb-2"></div>
            <div class="skeleton skeleton-text" style="width: 60%;"></div>
        </div>
    `.repeat(count);
    
    container.innerHTML = skeletonHTML;
}

// ========== Enhanced Button Click Effects ==========
function addRippleEffect(button) {
    button.addEventListener('click', function(e) {
        const ripple = document.createElement('span');
        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;
        
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';
        ripple.classList.add('ripple');
        
        this.appendChild(ripple);
        
        setTimeout(() => ripple.remove(), 600);
    });
}

// ========== Form Validation with Animation ==========
function validateForm(form) {
    const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('is-invalid');
            input.style.animation = 'shake 0.5s';
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
        }
    });
    
    return isValid;
}

// ========== Shake Animation ==========
const shakeKeyframes = `
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        10%, 30%, 50%, 70%, 90% { transform: translateX(-10px); }
        20%, 40%, 60%, 80% { transform: translateX(10px); }
    }
`;

const style = document.createElement('style');
style.textContent = shakeKeyframes;
document.head.appendChild(style);

// ========== Confetti Effect ==========
function createConfetti() {
    const colors = ['#0084ff', '#00c6ff', '#22c55e', '#fbbf24', '#ef4444'];
    const confettiCount = 50;
    
    for (let i = 0; i < confettiCount; i++) {
        const confetti = document.createElement('div');
        confetti.style.position = 'fixed';
        confetti.style.width = '10px';
        confetti.style.height = '10px';
        confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
        confetti.style.left = Math.random() * 100 + '%';
        confetti.style.top = '-10px';
        confetti.style.borderRadius = '50%';
        confetti.style.pointerEvents = 'none';
        confetti.style.zIndex = '9999';
        confetti.style.animation = `confettiFall ${Math.random() * 3 + 2}s linear forwards`;
        
        document.body.appendChild(confetti);
        
        setTimeout(() => confetti.remove(), 5000);
    }
}

// Add confetti animation
const confettiStyle = document.createElement('style');
confettiStyle.textContent = `
    @keyframes confettiFall {
        to {
            transform: translateY(100vh) rotate(360deg);
            opacity: 0;
        }
    }
`;
document.head.appendChild(confettiStyle);

// ========== Progress Update with Animation ==========
function updateProgressWithAnimation(progressBar, newPercent) {
    const currentPercent = parseInt(progressBar.style.width) || 0;
    const duration = 1000;
    const startTime = performance.now();
    
    function animate(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        // Easing function
        const easeOutCubic = 1 - Math.pow(1 - progress, 3);
        const currentPercentAnimated = currentPercent + (newPercent - currentPercent) * easeOutCubic;
        
        progressBar.style.width = currentPercentAnimated + '%';
        
        if (progress < 1) {
            requestAnimationFrame(animate);
        } else {
            progressBar.style.width = newPercent + '%';
        }
    }
    
    requestAnimationFrame(animate);
}

// ========== Enroll Button Enhancement ==========
function enhanceEnrollButton(button) {
    if (!button) return;
    
    button.addEventListener('click', async function() {
        const pathId = this.dataset.pathId;
        if (!pathId) return;
        
        // Disable button and show loading
        this.disabled = true;
        const originalHTML = this.innerHTML;
        this.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Enrolling...';
        this.style.opacity = '0.7';
        
        try {
            const response = await fetch(`/api/v1/learning-paths/${pathId}/enroll`, {
                method: 'POST'
            });
            
            const data = await response.json();
            
            if (response.ok && data.status === 'success') {
                // Success animation
                this.innerHTML = '<i class="fas fa-check me-2"></i>Enrolled!';
                this.classList.add('btn-success');
                showToast('Successfully enrolled in learning path!', 'success');
                
                // Confetti effect
                createConfetti();
                
                // Reload after short delay
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            } else {
                // Error state
                this.innerHTML = originalHTML;
                this.disabled = false;
                this.style.opacity = '1';
                showToast(data.message || 'Failed to enroll. Please try again.', 'error');
            }
        } catch (error) {
            console.error('Enroll error:', error);
            this.innerHTML = originalHTML;
            this.disabled = false;
            this.style.opacity = '1';
            showToast('Please login to enroll in learning paths', 'error');
        }
    });
}

// ========== Generate Form Enhancement ==========
function enhanceGenerateForm() {
    const form = document.getElementById('generate-form');
    if (!form) return;
    
    // Add ripple to submit button
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        addRippleEffect(submitBtn);
    }
    
    // Enhanced form submission
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!validateForm(this)) {
            showToast('Please fill in all required fields', 'error');
            return;
        }
        
        const formData = new FormData(this);
        const goal = formData.get('goal');
        const difficultyLevel = formData.get('difficultyLevel') || null;
        const maxTutorials = parseInt(formData.get('maxTutorials')) || 10;
        const estimatedHours = formData.get('estimatedHours') ? parseInt(formData.get('estimatedHours')) : null;
        
        const selectedCategories = Array.from(
            document.querySelectorAll('input[name="preferredCategoryIds"]:checked')
        ).map(cb => parseInt(cb.value));
        
        const requestData = {
            goal: goal,
            difficultyLevel: difficultyLevel,
            maxTutorials: maxTutorials,
            preferredCategoryIds: selectedCategories.length > 0 ? selectedCategories : null,
            estimatedHours: estimatedHours
        };
        
        // Show loading overlay
        const loadingOverlay = document.getElementById('loading-overlay');
        if (loadingOverlay) {
            loadingOverlay.style.display = 'flex';
        }
        
        try {
            const response = await fetch('/api/v1/learning-paths/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            });
            
            const data = await response.json();
            
            if (response.ok && data.status === 'success' && data.data) {
                const pathId = data.data.recommendedPath.id;
                
                // Success animation
                if (loadingOverlay) {
                    loadingOverlay.querySelector('.spinner').style.display = 'none';
                    loadingOverlay.querySelector('h3').textContent = 'Path Generated Successfully!';
                    loadingOverlay.querySelector('p').innerHTML = 
                        '<div class="checkmark"><i class="fas fa-check"></i></div>';
                }
                
                // Confetti
                createConfetti();
                
                // Redirect after delay
                setTimeout(() => {
                    window.location.href = `/tutorials/learning-paths/${pathId}`;
                }, 1500);
            } else {
                if (loadingOverlay) {
                    loadingOverlay.style.display = 'none';
                }
                showToast(data.message || 'Failed to generate learning path. Please try again.', 'error');
            }
        } catch (error) {
            console.error('Generation error:', error);
            if (loadingOverlay) {
                loadingOverlay.style.display = 'none';
            }
            showToast('Please login to generate learning paths', 'error');
        }
    });
}

// ========== Initialize on DOM Load ==========
document.addEventListener('DOMContentLoaded', function() {
    // Observe cards for scroll animations
    observeCards();
    
    // Enhance enroll buttons
    const enrollButtons = document.querySelectorAll('#enroll-btn, [data-path-id]');
    enrollButtons.forEach(btn => {
        if (btn.id === 'enroll-btn' || btn.hasAttribute('data-path-id')) {
            enhanceEnrollButton(btn);
        }
    });
    
    // Enhance generate form
    enhanceGenerateForm();
    
    // Add ripple to all buttons
    document.querySelectorAll('.btn-primary, .btn-success').forEach(btn => {
        addRippleEffect(btn);
    });
    
    // Animate progress bars
    document.querySelectorAll('.progress-fill').forEach(bar => {
        const percent = parseInt(bar.style.width) || 0;
        if (percent > 0) {
            bar.style.width = '0%';
            setTimeout(() => {
                updateProgressWithAnimation(bar, percent);
            }, 300);
        }
    });
    
    // Animate counters
    document.querySelectorAll('.stat-value, .h4').forEach(element => {
        const text = element.textContent.trim();
        const number = parseInt(text);
        if (!isNaN(number) && number > 0) {
            animateCounter(element, number);
        }
    });
    
    // Add hover sound effect (optional - can be disabled)
    // document.querySelectorAll('.path-card, .step-card').forEach(card => {
    //     card.addEventListener('mouseenter', () => {
    //         // Subtle hover effect
    //     });
    // });
});

// ========== Export functions for global use ==========
window.LearningPaths = {
    showToast,
    smoothScrollTo,
    animateProgressBar,
    createConfetti,
    updateProgressWithAnimation,
    enhanceEnrollButton
};

