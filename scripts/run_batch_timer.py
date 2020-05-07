#!/usr/bin/env python3

import argparse
import os
import re
import sys
import logging
import subprocess
from pathlib import Path
from queue import Queue
from threading import Thread

SINGULARITY_DIRNAME_PATTERN = re.compile(
    r'(.*)\[.*\]\[ioId=\d+,seed=(\d+)\].*')


class Worker(Thread):
    """ Thread executing tasks from a given tasks queue """

    def __init__(self, tasks):
        Thread.__init__(self)
        self.tasks = tasks
        self.daemon = True
        self.start()

    def run(self):
        while True:
            func, args, kargs = self.tasks.get()
            try:
                func(*args, **kargs)
            except Exception as e:
                # An exception happened in this thread
                print(e)
            finally:
                # Mark this task as done, whether an exception happened or not
                self.tasks.task_done()


class ThreadPool:
    """ Pool of threads consuming tasks from a queue """

    def __init__(self, num_threads):
        self.tasks = Queue(num_threads)
        for _ in range(num_threads):
            Worker(self.tasks)

    def add_task(self, func, *args, **kargs):
        """ Add a task to the queue """
        self.tasks.put((func, args, kargs))

    def map(self, func, args_list):
        """ Add a list of tasks to the queue """
        for args in args_list:
            self.add_task(func, args)

    def wait_completion(self):
        """ Wait for completion of all the tasks in the queue """
        self.tasks.join()


def check_paths(input_dirs):
    for input_dir in input_dirs:
        if not (input_dir.exists() and input_dir.is_dir()):
            raise ValueError('Directory {} does not exist'.format(input_dir))


def get_timer_from_bench_name(bench_name, use_int):
    name_split = bench_name.split('_')
    timer_name = '{}_{}'.format(name_split[0], name_split[1]) if name_split[
        0] == 'pcre' else name_split[0]
    if use_int and not timer_name.startswith('pcre'):
        timer_name = timer_name + '_int'
    return '{}_timer'.format(timer_name)


def timer_task(bench_name, bench_trial, cmd):
    logging.warning('Timing {} (trial {})'.format(bench_name, bench_trial))
    subprocess.run(cmd.split(), check=True,
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def schedule_timer(args, thread_pool):
    subdirs = sorted(args.resultdir.iterdir())
    if args.prefix is not None:
        subdirs = filter(lambda s: s.name.startswith(args.prefix), subdirs)
    for subdir in subdirs:
        match = SINGULARITY_DIRNAME_PATTERN.match(str(subdir.name))
        if match is None:
            raise ValueError(
                'Directory name not recognized: {}'.format(subdir.name))
        bench_name = match.group(1)
        bench_trial = match.group(2)

        outpath = args.output / bench_name
        os.makedirs(outpath, exist_ok=True)
        outname = outpath / 'seed_{}.csv'.format(bench_trial)
        timer_exec = args.timers / \
            get_timer_from_bench_name(bench_name, args.use_int)
        timer_cmd = '{} {} -t {} -o {}'.format(
            args.run_timer, subdir, timer_exec, outname)
        pool.add_task(timer_task, bench_name, bench_trial, timer_cmd)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Slowfuzz batch timer runner')
    parser.add_argument('resultdir', metavar='DIR',
                        type=Path, help='singularity result directory')
    parser.add_argument('-o', '--output', metavar='OUTDIR', required=True,
                        type=Path, help='Directory to store the output data.')
    parser.add_argument('-r', '--run-timer', metavar='RUN_TIMER',
                        help='Where to find the \'run_timer\' script', required=True)
    parser.add_argument('-t', '--timers', metavar='TIMER_DIR', type=Path,
                        required=True, help='Location of the timer programs')
    parser.add_argument('-p', '--prefix', metavar='PREFIX',
                        type=str, help='Only process benchmarks with this prefix')
    parser.add_argument('-i', '--use-int',
                        action='store_true', default=False, help='Use the "_int" variant of the timers')
    parser.add_argument('-j', '--jobs', metavar='JOB', type=int,
                        help='Simultaneous jobs to execute. Default to 1.', default=1)
    parser.add_argument('-d', '--debug',
                        help="Print lots of debugging statements",
                        action="store_const", dest="loglevel", const=logging.DEBUG,
                        default=logging.WARNING)
    parser.add_argument('-v', '--verbose',
                        help="Be verbose",
                        action="store_const", dest="loglevel", const=logging.INFO,
                        )
    args = parser.parse_args()
    logging.basicConfig(
        format='[%(levelname)s] %(message)s', level=args.loglevel)

    check_paths([args.resultdir, args.output, args.timers])
    pool = ThreadPool(args.jobs)
    schedule_timer(args, pool)
    pool.wait_completion()
