// Hero Client - Web Core
document.addEventListener('DOMContentLoaded', () => {
    initParticles();
    initScrollAnimations();
    animateNumbers();
});

// 1. Particle System (Lightweight Canvas Implementation)
function initParticles() {
    const canvas = document.createElement('canvas');
    canvas.id = 'particles';
    document.body.prepend(canvas);

    const ctx = canvas.getContext('2d');
    let width, height;
    let particles = [];

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    }

    class Particle {
        constructor() {
            this.x = Math.random() * width;
            this.y = Math.random() * height;
            this.vx = (Math.random() - 0.5) * 0.5;
            this.vy = (Math.random() - 0.5) * 0.5;
            this.size = Math.random() * 2;
            this.alpha = Math.random() * 0.5;
        }

        update() {
            this.x += this.vx;
            this.y += this.vy;

            if (this.x < 0) this.x = width;
            if (this.x > width) this.x = 0;
            if (this.y < 0) this.y = height;
            if (this.y > height) this.y = 0;
        }

        draw() {
            // Gold color for Hero Client
            ctx.fillStyle = `rgba(255, 215, 0, ${this.alpha})`;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    function init() {
        particles = [];
        for (let i = 0; i < 50; i++) {
            particles.push(new Particle());
        }
    }

    function animate() {
        ctx.clearRect(0, 0, width, height);
        particles.forEach(p => {
            p.update();
            p.draw();
        });
        requestAnimationFrame(animate);
    }

    window.addEventListener('resize', () => {
        resize();
        init();
    });

    resize();
    init();
    animate();
}

// 2. Scroll Reveal Animations
function initScrollAnimations() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('.tilt-element').forEach((el, index) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'all 0.6s cubic-bezier(0.5, 0, 0, 1)';
        el.style.transitionDelay = `${index * 100}ms`;
        observer.observe(el);
    });
}

// 3. Number Counter Animation
function animateNumbers() {
    const stats = document.querySelectorAll('.stat-number');
    stats.forEach(stat => {
        const target = +stat.getAttribute('data-target');
        const duration = 2000; // ms
        const increment = target / (duration / 16);

        let current = 0;
        const update = () => {
            current += increment;
            if (current < target) {
                stat.textContent = Math.ceil(current).toLocaleString();
                requestAnimationFrame(update);
            } else {
                stat.textContent = target.toLocaleString() + '+';
            }
        };
        update();
    });
}
// 4. Dashboard View Switching
function switchView(viewId, element) {
    // Hide all views
    document.querySelectorAll('.view-section').forEach(el => {
        el.style.display = 'none';
    });

    // Show selected view
    const view = document.getElementById(`view-${viewId}`);
    if (view) {
        view.style.display = 'block';
        // Trigger animations for the new view
        initScrollAnimations();
    } else {
        console.error(`View not found: view-${viewId}`);
    }

    // Update Sidebar Active State
    if (element) {
        document.querySelectorAll('.menu-item').forEach(el => el.classList.remove('active'));
        element.classList.add('active');
    }
}

// 5. License UI Update
function updateLicenseUI(plan, expiry) {
    const licenseStatus = document.getElementById('licenseStatus');
    const sidebarPlan = document.getElementById('sidebarPlan');
    const premiumMenu = document.getElementById('premiumMenu');
    const avatar = document.getElementById('avatarInitials');

    // Normalize plan string
    const safePlan = (plan || '').toLowerCase();
    const isPremium = safePlan.includes('premium') || safePlan.includes('lifetime') || safePlan.includes('pro') || safePlan.includes('vip');

    if (isPremium) {
        if (licenseStatus) {
            licenseStatus.innerHTML = '<span style="color: var(--success)"><i class="fa-solid fa-check-circle"></i> Active</span>';
        }
        if (sidebarPlan) {
            sidebarPlan.textContent = plan || 'Premium User';
            sidebarPlan.style.color = 'var(--primary)';
        }
        if (premiumMenu) {
            premiumMenu.style.display = 'flex';
        }
        if (avatar) {
            avatar.style.background = 'linear-gradient(135deg, var(--primary), var(--secondary))';
            avatar.style.color = 'black';
        }
    } else {
        if (licenseStatus) {
            licenseStatus.textContent = 'Free Version';
        }
        if (sidebarPlan) {
            sidebarPlan.textContent = 'Free User';
        }
        if (premiumMenu) {
            premiumMenu.style.display = 'none';
        }
    }
}

// Expose functions to global scope for HTML onclick attributes
window.switchView = switchView;
window.updateLicenseUI = updateLicenseUI;
