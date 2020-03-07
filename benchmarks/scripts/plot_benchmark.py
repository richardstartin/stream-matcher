import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

def rename_columns(name):
    if name == 'Score Error (99.9%)':
        return 'error'
    else:
        return str.lower(name).replace('param: ', '')


def plot(input, output, searcher_type):
    df = pd.read_csv(input)
    df = df.rename(rename_columns, axis='columns')
    df = df.drop(['benchmark', 'mode', 'threads', 'samples', 'seed', 'unit'], axis=1)
    df.set_index('searchertype')
    df = df.loc[(df['searchertype'] == f'{searcher_type}')
                & (df['logvariety'] == 12)
                & (df['datalength'] == 2000)]\
        .drop(['searchertype', 'logvariety', 'datalength'], axis=1)

    df = df.pivot(index='termlength', columns='dataset')
    row = df['score'].max()
    upper = -1
    for i in range(len(row)):
        upper = max(row[i], upper)
    ax = df.plot.bar(y='score', yerr='error', title=f'{searcher_type} (μs/op, datalength=2000)')
    ax.set_ylim([0, 2 * upper])
    ax.get_figure().savefig(f'{output}.png')


def plot2(input, output):
    df = pd.read_csv(input)
    df = df.rename(rename_columns, axis='columns')
    df = df.drop(['benchmark', 'mode', 'threads', 'samples', 'seed', 'unit'], axis=1)
    df.set_index('searchertype')
    df = df.loc[(df['termlength'] == 40)
                & (df['logvariety'] == 12)
                & (df['datalength'] == 2000)]\
        .drop(['termlength', 'logvariety', 'datalength'], axis=1)

    df = df.pivot(index='dataset', columns='searchertype')
    print(df)
    row = df['score'].max()
    upper = -1
    for i in range(len(row)):
        upper = max(row[i], upper)
    ax = df.plot.bar(y='score', yerr='error', title='heuristics (μs/op, termlength=40, datalength=2000)')
    ax.set_ylim([0, 1.5 * upper])
    plt.setp(ax.get_xticklabels(), rotation=270, ha="right", rotation_mode="anchor")
    fig = ax.get_figure()
    fig.savefig(f'{output}.png')


plot('../output/searcher.csv', 'benchmark', 'UNSAFE_SPARSE_BIT_MATRIX_SWAR')
plot2('../output/swar-bitsliced.csv', 'swar-bitsliced')
