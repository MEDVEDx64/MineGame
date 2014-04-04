/// medvedx64 2013 2 13
/// Rewrite to Tworojok64 entropy test, 2014-04-03

#include <SDL/SDL_gfxPrimitives.h>
#include <stdio.h>
#include <math.h>

static SDL_Surface * screen = NULL;

#define SW 800
#define SH 512

int init_gfx()
{
	if(SDL_Init(SDL_INIT_VIDEO))
	{
		printf("SDL_Init fail.\n");
		return 1;
	}

	screen = SDL_SetVideoMode(SW, SH, 0, SDL_DOUBLEBUF|SDL_HWSURFACE);
	if(!screen)
	{
		printf("Failed to create the screen.\n");
		return 1;
	}

	return 0;
}

void WaitTilQuit()
{
	SDL_Event ev;
	int a = 1;
	while(a)
	{
		if(SDL_PollEvent(&ev))
		{
			if(ev.type == SDL_QUIT)
				a = 0;
		}
		else
			SDL_Delay(100);
	}
}

#define ZERO_X (SW/2)
#define ZERO_Y (SH/2)

#define BGCOLOR 0xbbbbbbff
#define LINE_COLOR 0x002233ff

#define STEP 2

void draw_bg()
{
	boxColor(screen, 0, 0, SW-1, SH-1, BGCOLOR);
	lineColor(screen, 0, ZERO_Y, SW-1, ZERO_Y, LINE_COLOR);
	lineColor(screen, ZERO_X, 0, ZERO_X, SH-1, LINE_COLOR);

	// text
	stringColor(screen, ZERO_X+4, ZERO_Y+4, "0", LINE_COLOR);
	stringColor(screen, SW-12, ZERO_Y+4, "x", LINE_COLOR);
	stringColor(screen, ZERO_X+4, SH-12, "y", LINE_COLOR);
}

/*
void draw_intervals_subr(int is_y, int is_upward)
{
	int step_ = 0;
	int x; for(x = is_upward ? ZERO_X : 0; x < is_upward ? SW : ZERO_X; x++)
	{
		if(step_ == STEP)
		{
			lineColor(screen, is_y ? ZERO_Y : x,
				is_y ? ZERO_Y : x, is_y ? x : ZERO_X, x ? ZERO_X-4 : x-4,
				LINE_COLOR);
			step_ = 0;
		}
		++step_;
	}
}
*/

void draw_intervals()
{
/*	draw_intervals_subr(0,0);
	draw_intervals_subr(1,0);
	draw_intervals_subr(0,1);
	draw_intervals_subr(1,1);	*/

	int step_ = 0;
	int x; for(x = ZERO_X; x < SW; x++)
	{
		if(step_ == STEP)
		{
			lineColor(screen, x, ZERO_Y, x, ZERO_Y-4,
				LINE_COLOR);
			step_ = 0;
		}
		++step_;
	}
}

#define GRAPH_COLOR 0xaa1100ff

void draw_func(int (*y)(int x))
{
	int i;
	for(i = -ZERO_X; i < ZERO_X; i++)
	{
		//printf("%d %d\n", xf(i), yf(i));
		lineColor(screen, (i+ZERO_X)*STEP, y(i)+ZERO_Y, (i+1+ZERO_X)*STEP, y(i+1)+ZERO_Y, GRAPH_COLOR);
	}
}

void draw_mouse_tip(int x, int y)
{
	boxColor(screen, x+10, y+10, x+10+80, y+10+12, 0x55);
	rectangleColor(screen, x+10, y+10, x+10+80, y+10+12, 0xaa);

	char buf[0x1000];
	sprintf(buf, "%d, %d", (x-ZERO_X)/STEP, y-ZERO_Y);
	stringColor(screen, x+13, y+13, buf, 0x770000bb);
}

long file_length = 0;
int32_t *graph_data;
int read_numbers(const char *fname) {
	FILE *f = fopen(fname, "r");
	if(f == NULL) {
		fprintf(stderr, "Warning: graph file couldn't be read: %s\n", fname);
		return -1;
	}

	char *buffer = malloc(0x4000);
	int i = 0;
	while(1) {
		int elements_read = fscanf(f, "%s\n", buffer);
		if(elements_read <= 0) break;
		graph_data[i] = atoi(buffer);
		++i;
	}

	free(buffer);
	return 0;
}

//int das_func(int v) { return 10*cos(v);}
#define SCALE_FACTOR 0.055f
int das_func(int v) { return graph_data[v+SW/2]*SCALE_FACTOR; }

int main(int c, char **v)
{
	if(init_gfx()) return 1;

	graph_data = malloc(4*SW);
	memset(graph_data, 0, 4*SW);
	if(c > 1) read_numbers(v[1]);

	int run = 1;
	while(run)
	{
		int mx = 0;
		int my = 0;
		SDL_Event ev;
		if(SDL_PollEvent(&ev))
		{
			mx = ev.button.x;
			my = ev.button.y;
			if(ev.type == SDL_QUIT) run = 0;
			draw_bg();
			//draw_intervals();
			draw_func(das_func);
			draw_mouse_tip(mx, my);
			SDL_Flip(screen);
		}
		else SDL_Delay(16);
	}
	//WaitTilQuit();

	free(graph_data);
	return 1;
}
