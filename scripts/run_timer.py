#!/usr/bin/env python3

import argparse
import re
import sys
import logging
import subprocess
import contextlib
from pathlib import Path

SINGULARITY_FILENAME_PATTERN = re.compile(r'bestInput\[time=(\d+)\].*')


@contextlib.contextmanager
def smart_open(filename=None):
    if filename and filename != '-':
        fh = open(filename, 'w')
    else:
        fh = sys.stdout

    try:
        yield fh
    finally:
        if fh is not sys.stdout:
            fh.close()


def check_paths(input_dir, timer_file):
    if not (input_dir.exists() and input_dir.is_dir()):
        raise ValueError('Input directory {} does not exist'.format(input_dir))
    if not (timer_file.exists() and timer_file.is_file()):
        raise ValueError('Timer file {} does not exist'.format(timer_file))


def get_xvalue(path):
    match = SINGULARITY_FILENAME_PATTERN.match(str(path.resolve().name))
    if match is None:
        return None
    return int(match.group(1))


def parse_benchmark_result(output):
    lines = output.strip().split('\n')
    cpu_time_str = None
    for line in lines:
        line_splits = line.split(',')
        if line_splits[0].endswith('_mean"'):
            cpu_time_str = line_splits[3]
            break
    if cpu_time_str is None:
        return None
    try:
        return float(cpu_time_str)
    except ValueError:
        return None


def get_yvalue(path, timer_path):
    cmd = '{} {}'.format(timer_path, path)
    result = subprocess.run(
        cmd.split(), stdout=subprocess.PIPE, universal_newlines=True)
    if result.returncode != 0:
        return None
    return parse_benchmark_result(result.stdout)


def collect_data(args):
    slow_units = list(args.datafile.glob('bestInput*'))
    ret = dict()
    for path in slow_units:
        x_value = get_xvalue(path)
        y_value = get_yvalue(path, args.timer)
        if x_value is not None and y_value is not None:
            ret[x_value] = y_value
    return ret


def write_data(raw_data, out_file):
    with smart_open(out_file) as f:
        for x, y in sorted(raw_data.items()):
            print('{}, {}'.format(x, y), file=f)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Singularity result timer')
    parser.add_argument('datafile', metavar='FILE', type=Path,
                        help='slowfuzz result directory')
    parser.add_argument('-o', '--output', metavar='OUT',
                        help='File to store the output data')
    parser.add_argument('-t', '--timer', metavar='TIMER', type=Path,
                        help='Timer program of all the inputs', required=True)
    parser.add_argument('-d', '--debug',
                        help="Print lots of debugging statements",
                        action="store_const", dest="loglevel", const=logging.DEBUG,
                        default=logging.WARNING)
    parser.add_argument('-v', '--verbose',
                        help="Be verbose",
                        action="store_const", dest="loglevel", const=logging.INFO,
                        )
    args = parser.parse_args()
    logging.basicConfig(format='%(levelname)s:%(message)s', level=args.loglevel)

    try:
        check_paths(args.datafile, args.timer)
        raw_data = collect_data(args)
        write_data(raw_data, args.output)
    except ValueError as e:
        print('[ERROR] {}'.format(e), file=sys.stderr)
        sys.exit(-1)
